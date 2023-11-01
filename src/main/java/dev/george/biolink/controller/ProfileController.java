package dev.george.biolink.controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.george.biolink.entity.UserDetailsEntity;
import dev.george.biolink.model.*;
import dev.george.biolink.model.type.LogType;
import dev.george.biolink.repository.LogsRepository;
import dev.george.biolink.repository.PendingRedirectTransferRepository;
import dev.george.biolink.repository.ProfileRepository;
import dev.george.biolink.repository.RedirectRepository;
import dev.george.biolink.schema.profile.UpdateEmailSchema;
import dev.george.biolink.service.JwtService;
import dev.george.biolink.service.MfaService;
import dev.george.biolink.service.UserRankCheckService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Date;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
@RestController
public class ProfileController {

    private final Gson gson;
    private final JwtService jwtService;
    private final LogsRepository logsRepository;
    private final MfaService mfaService;
    private final PendingRedirectTransferRepository redirectTransferRepository;
    private final ProfileRepository profileRepository;
    private final RedirectRepository redirectRepository;
    private final UserRankCheckService rankCheckService;

    @GetMapping(
            value = "/profile/get-basic-data",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> getProfileData() {
        JsonObject object = new JsonObject();
        JsonObject profileData = new JsonObject();

        UserDetailsEntity userDetails = (UserDetailsEntity) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        Profile profile = userDetails.getProfile();

        List<Rank> userGroups = rankCheckService.getGroups(profile.getId());
        List<Redirect> redirects = redirectRepository.findByUserId(profile.getId());
        List<PendingRedirectTransfer> redirectTransfers = redirectTransferRepository.findAllByPendingRedirectStringIn(
                redirects.stream().map(Redirect::getRedirectString).toList()
        );

        JsonArray groupsArray = new JsonArray();
        JsonArray redirectsArray = new JsonArray();
        JsonArray redirectTransfersArray = new JsonArray();

        userGroups.forEach(group -> groupsArray.add(group.getName()));
        redirects.forEach(redirect -> redirectsArray.add(redirect.getRedirectString()));

        redirectTransfers.forEach(redirectTransfer -> {
            JsonObject redirectTransferObject = new JsonObject();

            redirectTransferObject.addProperty("redirect", redirectTransfer.getPendingRedirectString());
            redirectTransferObject.addProperty("transferringTo", Optional.of(redirectTransfer.getTransferringTo())
                    .map(profileRepository::findById)
                    .flatMap(userProfile -> userProfile.map(Profile::getUsername))
                    .orElse(null));

            redirectTransfersArray.add(redirectTransferObject);
        });

        profileData.addProperty("email", profile.getEmail());
        profileData.addProperty("username", profile.getUsername());
        profileData.addProperty("mfa_enabled", profile.getMfaEnabled());
        profileData.addProperty("max_redirects", rankCheckService.getMaxRedirects(userGroups));
        profileData.addProperty("created_at", profile.getCreatedAt().getTime());
        profileData.addProperty("last_login", profile.getLastLogin().getTime());
        profileData.addProperty("invited_by", Optional.of(profile.getInvitedBy())
                .map(profileRepository::findById)
                .flatMap(userProfile -> userProfile.map(Profile::getUsername))
                .orElse(null));

        profileData.add("groups", groupsArray);
        profileData.add("redirects", redirectsArray);
        profileData.add("redirectTransfers", redirectTransfersArray);

        object.addProperty("success", true);
        object.add("data", profileData);

        return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(200));
    }

    @PutMapping(
            value = "/profile/update-email",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> updateEmail(
            UpdateEmailSchema schema
    ) {
        JsonObject object = new JsonObject();

        Profile profile = ((UserDetailsEntity) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal()).getProfile();

        if (schema.getContextToken() == null) {
            String newEmail = schema.getNewEmail();

            if (profileRepository.findOneByEmail(newEmail).isPresent()) {
                object.addProperty("error", true);
                object.addProperty("error_code", "email_in_use");

                return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(400));
            }

            Map<String, Object> claims = new HashMap<>();

            claims.put("newEmail", newEmail);
            claims.put("requiredMfaType", mfaService.getVerificationMethod(profile));

            String jwtToken = mfaService.createJwtContext(claims, profile.getId());

            object.addProperty("error", true);
            object.addProperty("error_code", "additional_mfa_required");
            object.addProperty("context_token", jwtToken);

            return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(403));
        }

        try {
            if (Date.from(Instant.now()).toInstant().isAfter(
                    jwtService.getExpirationDateFromToken(schema.getContextToken()).toInstant())) {
                object.addProperty("error", true);
                object.addProperty("error_code", "invalid_context_id");

                return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(400));
            }

            if (!mfaService.verifyMfa(schema.getVerificationCode(), schema.getContextToken(), profile)) {
                object.addProperty("error", true);
                object.addProperty("error_code", "invalid_mfa_code");

                return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(403));
            }

            String email = (String) jwtService.getClaimFromToken(schema.getContextToken(),
                    (claims) -> claims.get("newEmail"));

            if (email == null) {
                object.addProperty("error", true);
                object.addProperty("error_code", "missing_email");

                return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(400));
            }

            profile.setEmail(email);

            Log log = new Log();

            log.setLogTypeId(LogType.USER_CHANGE_EMAIL.getType());
            log.setTargetUser(profile.getId());

            logsRepository.saveAndFlush(log);
            profileRepository.saveAndFlush(profile);

            object.addProperty("success", true);

            return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(200));
        } catch (Exception exc) {
            object.addProperty("error", true);
            object.addProperty("error_code", "invalid_context_id");

            return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(200));
        }
    }
}
