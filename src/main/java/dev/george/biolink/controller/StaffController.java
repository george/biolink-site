package dev.george.biolink.controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.george.biolink.entity.UserDetailsEntity;
import dev.george.biolink.model.*;
import dev.george.biolink.model.type.LogType;
import dev.george.biolink.repository.*;
import dev.george.biolink.schema.staff.BanUserSchema;
import dev.george.biolink.schema.staff.ChangeUserEmailSchema;
import dev.george.biolink.schema.staff.ChangeUserUsernameSchema;
import dev.george.biolink.schema.staff.ReassignRedirectSchema;
import dev.george.biolink.service.JwtService;
import dev.george.biolink.service.UserRankCheckService;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@RestController
public class StaffController {

    private final BansRepository bansRepository;
    private final BanReasonRepository banReasonRepository;
    private final Gson gson;
    private final JwtService jwtService;
    private final LogsRepository logsRepository;
    private final NoteRepository noteRepository;
    private final PendingRedirectTransferRepository redirectTransferRepository;
    private final ProfileRepository profileRepository;
    private final RankRepository rankRepository;
    private final RedirectRepository redirectRepository;
    private final UserRankCheckService rankCheckService;

    @PutMapping(
            value = "/staff/assign-redirect",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> reassignRedirect(
            ReassignRedirectSchema schema
    ) {
        JsonObject object = new JsonObject();

        if (schema.getRedirect() == null || schema.getRedirect().length() > 32) {
            object.addProperty("error", true);
            object.addProperty("error_code", "redirect_null_or_too_large");

            return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(200));
        }

        Log log = new Log();

        UserDetailsEntity userDetails = (UserDetailsEntity) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        log.setStaffId(userDetails.getProfile().getId());
        log.setTargetUser(schema.getNewProfileId());
        log.setLogTypeId(LogType.ASSIGN_PROFILE_REDIRECT.getType());

        redirectTransferRepository.deleteByPendingRedirectString(schema.getRedirect().toLowerCase());
        redirectRepository.findByRedirectString(schema.getRedirect().toLowerCase())
                .ifPresentOrElse((redirect) -> {
                    log.setDescription("Reassigned from user " + redirect.getUserId());

                    redirect.setUserId(schema.getNewProfileId());
                }, () -> {
                    log.setDescription("Created new redirect");

                    Redirect redirect = new Redirect();

                    redirect.setRedirectString(schema.getRedirect().toLowerCase());
                    redirect.setUserId(schema.getNewProfileId());

                    redirectRepository.saveAndFlush(redirect);
                });

        object.addProperty("success", true);

        logsRepository.saveAndFlush(log);

        return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(200));
    }

    @PutMapping(
            value = "/staff/change-username",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> changeUserUsername(
            ChangeUserUsernameSchema schema
    ) {
        JsonObject object = new JsonObject();

        UserDetailsEntity userDetails = (UserDetailsEntity) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        Optional<Profile> targetProfileOptional = profileRepository.findById(schema.getUserId());

        if (targetProfileOptional.isEmpty() || !targetProfileOptional.orElse(null).equals(
                profileRepository.findOneByUsername(schema.getCurrentUsername()).orElse(null))) {
            object.addProperty("error", true);
            object.addProperty("error_code", "user_not_found");

            return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(400));
        } else {
            if (!rankCheckService.isUserGroupHigher(userDetails.getProfile(), targetProfileOptional.get())) {
                object.addProperty("error", true);
                object.addProperty("error_code", "target_group_too_high");

                return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(403));
            }
        }

        Profile targetProfile = targetProfileOptional.get();
        targetProfile.setUsername(schema.getNewUsername());

        profileRepository.saveAndFlush(targetProfile);

        object.addProperty("success", true);

        return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(200));
    }

    @PutMapping(
            value = "/staff/change-email",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> changeUserEmail(
            ChangeUserEmailSchema schema
    ) {
        JsonObject object = new JsonObject();

        UserDetailsEntity userDetails = (UserDetailsEntity) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        Optional<Profile> targetProfileOptional = profileRepository.findById(schema.getUserId());

        if (targetProfileOptional.isEmpty() || !targetProfileOptional.get()
                .equals(profileRepository.findOneByEmail(schema.getOldEmail()).orElse(null))) {
            object.addProperty("error", true);
            object.addProperty("error_code", "user_not_found");

            return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(400));
        } else {
            if (!rankCheckService.isUserGroupHigher(userDetails.getProfile(), targetProfileOptional.get())) {
                object.addProperty("error", true);
                object.addProperty("error_code", "target_group_too_high");

                return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(403));
            }
        }

        if (profileRepository.findOneByEmail(schema.getNewEmail()).isPresent()) {
            object.addProperty("error", true);
            object.addProperty("error_code", "user_with_email_exists");

            return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(400));
        }

        Profile targetProfile = targetProfileOptional.get();
        targetProfile.setEmail(schema.getNewEmail());

        profileRepository.save(targetProfile);

        object.addProperty("success", true);

        return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(200));
    }

    @GetMapping(
            value = "/staff/get-profile",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> getUserProfile(
            @RequestParam int userId,
            @RequestParam String accessToken
    ) {
        JsonObject object = new JsonObject();
        Optional<Profile> profileOptional = profileRepository.findById(userId);

        if (profileOptional.isEmpty()) {
            object.addProperty("error", true);
            object.addProperty("error_code", "no_user_found");

            return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(400));
        }

        try {
            Instant expirationInstant = jwtService.getExpirationDateFromToken(accessToken).toInstant();

            if (System.currentTimeMillis() > expirationInstant.toEpochMilli() + 60L * 1000L * 5L) {
                object.addProperty("error", true);
                object.addProperty("error_code", "access_token_expired");

                return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(403));
            }

            List<Integer> allowedUsers = Arrays.stream(((String) jwtService.getClaimFromToken(accessToken,
                    (claim) -> claim.get("authorized-users"))).replace("[", "")
                    .replace("]", "")
                    .split(", "))
                    .mapToInt(Integer::parseInt)
                    .boxed()
                    .toList();

            if (!allowedUsers.contains(userId)) {
                object.addProperty("error", true);
                object.addProperty("error_code", "not_allowed_to_view_user");

                return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(403));
            }
        } catch (Exception exc) {
            object.addProperty("error", true);
            object.addProperty("error_code", "invalid_jwt_token");

            return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(403));
        }

        Optional<Profile> targetProfileOptional = profileRepository.findById(userId);

        if (targetProfileOptional.isEmpty()) {
            object.addProperty("error", true);
            object.addProperty("error_code", "user_not_found");

            return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(400));
        }

        JsonObject profileData = new JsonObject();
        Profile targetProfile = targetProfileOptional.get();

        List<Rank> groups = rankCheckService.getGroups(targetProfile.getId());

        JsonArray userGroups = new JsonArray();

        groups.forEach(group -> {
            JsonObject groupObject = new JsonObject();

            groupObject.addProperty("id", group.getId());
            groupObject.addProperty("name", group.getName());

            userGroups.add(groupObject);
        });

        JsonArray userNotesArray = new JsonArray();
        List<Note> userNotes = noteRepository.findAlByUserId(userId);

        List<Profile> staffProfiles = profileRepository.findAllById(
                userNotes.stream().map(Note::getStaffId).collect(Collectors.toSet())
        );

        userNotes.forEach(note -> {
            JsonObject noteObject = new JsonObject();

            noteObject.addProperty("noteId", note.getNoteId());
            noteObject.addProperty("staffUsername", staffProfiles.stream()
                    .filter(staffProfile -> staffProfile.getId() == note.getStaffId())
                    .findFirst()
                    .map(Profile::getUsername)
                    .orElse("Unknown User"));
            noteObject.addProperty("timestamp", note.getLeftAt().getTime());
            noteObject.addProperty("message", note.getNote());

            userNotesArray.add(noteObject);
        });

        JsonArray userRedirects = new JsonArray();

        redirectRepository.findByUserId(targetProfile.getId()).forEach(redirect ->
                userRedirects.add(redirect.getRedirectString()));

        profileData.addProperty("id", targetProfile.getId());
        profileData.addProperty("email", targetProfile.getEmail());
        profileData.addProperty("username", targetProfile.getUsername());
        profileData.addProperty("mfa_enabled", targetProfile.getMfaEnabled());
        profileData.addProperty("joinedAt", targetProfile.getCreatedAt().getTime());
        profileData.addProperty("lastLogin", targetProfile.getLastLogin().getTime());

        profileData.add("groups", userGroups);
        profileData.add("notes", userNotesArray);
        profileData.add("redirects", userRedirects);

        object.add("data", profileData);

        return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(200));
    }

    @GetMapping(
            value = "/staff/get-groups",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> getRanks() {
        JsonArray array = new JsonArray();

        rankRepository.findAll().forEach(rank -> {
            JsonObject object = new JsonObject();

            object.addProperty("id", rank.getId());
            object.addProperty("name", rank.getName());
            object.addProperty("style", rank.getStyle());

            object.addProperty("priority", rank.getPriority());
            object.addProperty("purchasable", rank.getPurchasable());

            object.addProperty("canManageLower", rank.getCanManageLower());
            object.addProperty("staff", rank.getStaff());

            object.addProperty("canBan", rank.getCanBan());
            object.addProperty("canManageUsers", rank.getCanManageUsers());
            object.addProperty("canGiveRanks", rank.getCanGiveRanks());
            object.addProperty("canGiveStaffRanks", rank.getCanGiveRanks());

            array.add(object);
        });

        return new ResponseEntity<>(gson.toJson(array), HttpStatusCode.valueOf(200));
    }

    @GetMapping(
            value = "/staff/get-user-groups",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> getGroups(
            @RequestParam int userId
    ) {
        JsonArray array = new JsonArray();

        rankCheckService.getGroups(userId).forEach(group -> {
            JsonObject object = new JsonObject();

            object.addProperty("id", group.getId());
            object.addProperty("name", group.getName());

            array.add(object);
        });

        return new ResponseEntity<>(gson.toJson(array), HttpStatusCode.valueOf(200));
    }

    @GetMapping(
            value = "/staff/profile",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> getProfile(
            @Nullable @RequestParam("userId") Integer userId,
            @Nullable @RequestParam("username") String username,
            @Nullable @RequestParam("redirect") String redirect,
            @Nullable @RequestParam("email") String email,
            @Nullable @RequestParam("lastIp") String lastIp
    ) {
        List<Profile> profiles = new ArrayList<>();
        Optional<Profile> optional = Optional.empty();

        JsonObject object = new JsonObject();

        if (userId != null) {
            optional = profileRepository.findById(userId);
        } else if (username != null) {
            optional = profileRepository.findOneByUsername(username);
        } else if (email != null) {
            optional = profileRepository.findOneByEmail(email);
        } else if (redirect != null) {
            Optional<Redirect> redirectInstance = redirectRepository.findByRedirectString(redirect);

            if (redirectInstance.isPresent()) {
                optional = profileRepository.findById(redirectInstance.get().getUserId());
            }
        } else if (lastIp != null) {
            String[] parts = lastIp.split("\\.");

            int ipNumbers = 0;
            for (int i = 0; i < 4; i++) {
                ipNumbers += Integer.parseInt(parts[i]) << (24 - (8 * i));
            }

            profiles.addAll(profileRepository.findAllByLastIp(ipNumbers));
        } else {
            object.addProperty("error", true);
            object.addProperty("error_code", "no_query_provided");

            return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(400));
        }

        if (optional.isEmpty() && profiles.isEmpty()) {
            object.add("users", new JsonArray());

            return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(204));
        } else optional.ifPresent(profiles::add);

        JsonArray users = new JsonArray();

        profiles.forEach(profile -> {
            JsonObject profileObject = new JsonObject();

            profileObject.addProperty("userId", profile.getId());
            profileObject.addProperty("username", profile.getUsername());

            users.add(profileObject);
        });

        Map<String, Object> claims = new HashMap<>();
        claims.put("authorized-users", Arrays.toString(profiles.stream().map(Profile::getId).toArray()));

        UserDetailsEntity userDetails = (UserDetailsEntity) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        object.addProperty("auth-token", jwtService.generateToken(claims,
                Integer.toString(userDetails.getProfile().getId())));

        Log log = new Log();

        log.setLogTypeId(LogType.GENERAL_USER_SEARCH.getType());
        log.setStaffId(userDetails.getProfile().getId());
        log.setDescription(String.format("Searched with query %d %s %s %s", userId, username, email, lastIp));

        logsRepository.saveAndFlush(log);

        return new ResponseEntity<>(gson.toJson(users), HttpStatusCode.valueOf(200));
    }

    @PostMapping(
            value = "/staff/ban-user",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> banUser(
            BanUserSchema schema
    ) {
        JsonObject object = new JsonObject();
        Optional<Profile> targetProfileOptional = profileRepository.findById(schema.getUserId());

        if (targetProfileOptional.isEmpty()) {
            object.addProperty("error", true);
            object.addProperty("error_code", "user_not_found");

            return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(400));
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsEntity userDetails = (UserDetailsEntity) authentication.getPrincipal();

        Profile profile = userDetails.getProfile();
        Profile targetProfile = targetProfileOptional.get();

        List<Rank> groups = userDetails.getGroups();

        if (groups.stream().noneMatch(Rank::getCanBan)) {
            object.addProperty("error", true);
            object.addProperty("error_code", "cannot_ban");

            return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(403));
        }

        if (!rankCheckService.isUserGroupHigher(profile, targetProfile)) {
            object.addProperty("error", true);
            object.addProperty("error_code", "target_group_too_high");

            return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(403));
        }

        if (bansRepository.findBansByUserId(targetProfile.getId()).stream().anyMatch(Ban::isBanActive)) {
            object.addProperty("error", true);
            object.addProperty("error_code", "user_ban_active");

            return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(400));
        }

        Log log = new Log();

        log.setStaffId(profile.getId());
        log.setTargetUser(targetProfile.getId());
        log.setDescription("Issued ban on user");
        log.setLogTypeId(LogType.BAN_USER.getType());

        logsRepository.saveAndFlush(log);

        int banReasonId = banReasonRepository.findByBanReason(schema.getReason())
                .orElseGet(() -> {
                    BanReason banReason = new BanReason();
                    banReason.setBanReason(schema.getReason());

                    return banReasonRepository.saveAndFlush(banReason);
                }).getBanId();

        Ban ban = new Ban();

        ban.setIssuedBy(profile.getId());
        ban.setUserId(targetProfile.getId());
        ban.setBanTypeId(banReasonId);

        bansRepository.saveAndFlush(ban);

        object.addProperty("success", true);

        return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(400));
    }

    @GetMapping(
            value = "/staff/unban-user",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> unbanUser(
            @RequestParam int userId
    ) {
        JsonObject object = new JsonObject();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsEntity userDetails = (UserDetailsEntity) authentication.getPrincipal();

        Profile profile = userDetails.getProfile();

        if (userDetails.getGroups().stream().noneMatch(Rank::getCanBan)) {
            object.addProperty("error", true);
            object.addProperty("error_code", "cannot_unban");

            return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(403));
        }

        List<Ban> bans = bansRepository.findBansByUserId(userId);
        Optional<Ban> currentBanOptional = bans.stream()
                .filter(Ban::isBanActive)
                .findFirst();

        if (currentBanOptional.isEmpty()) {
            object.addProperty("error", true);
            object.addProperty("error_code", "no_ban_active");

            return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(400));
        }

        Ban currentBan = currentBanOptional.get();
        currentBan.setBanActive(false);

        bansRepository.saveAndFlush(currentBan);

        Log log = new Log();

        log.setStaffId(profile.getId());
        log.setTargetUser(userId);
        log.setDescription("Unbanned user with ban ID " + currentBan.getPunishmentId());
        log.setLogTypeId(LogType.BAN_USER.getType());

        logsRepository.saveAndFlush(log);

        object.addProperty("success", true);

        return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(400));
    }
}
