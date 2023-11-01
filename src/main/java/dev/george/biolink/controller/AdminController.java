package dev.george.biolink.controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.george.biolink.entity.UserDetailsEntity;
import dev.george.biolink.entity.UserGroupId;
import dev.george.biolink.model.Log;
import dev.george.biolink.model.Profile;
import dev.george.biolink.model.Rank;
import dev.george.biolink.model.UserGroup;
import dev.george.biolink.model.type.LogType;
import dev.george.biolink.repository.*;
import dev.george.biolink.schema.admin.UpdateGroupSchema;
import dev.george.biolink.service.UserRankCheckService;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@AllArgsConstructor
public class AdminController {

    private final Gson gson;
    private final LogsRepository logsRepository;
    private final ProfileRepository profileRepository;
    private final RankRepository rankRepository;
    private final UserGroupsRepository groupsRepository;
    private final UserRankCheckService rankCheckService;

    @GetMapping(
            value = "/admin/get-logs",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> getLogs(
            @Nullable @RequestParam Integer targetId,
            @Nullable @RequestParam Integer staffId,
            @Nullable @RequestParam Integer logTypeId,
            @Nullable @NotNull @RequestParam int pageNumber,
            @NotNull @RequestParam int maxPerPage
    ) {
        JsonObject object = new JsonObject();

        Log exampleLog = new Log();

        if (targetId != null) {
            exampleLog.setTargetUser(targetId);
        }

        if (staffId != null) {
            exampleLog.setStaffId(staffId);
        }

        if (logTypeId != null) {
            exampleLog.setLogTypeId(logTypeId);
        }

        JsonArray logsArray = new JsonArray();

        Page<Log> page = logsRepository.findAll(Example.of(exampleLog, ExampleMatcher.matchingAny().withIgnoreNullValues()),
                PageRequest.of(pageNumber, maxPerPage));

        Map<Integer, String> usernames = new HashMap<>();
        Set<Integer> userIds = new HashSet<>();

        userIds.addAll(page.map(Log::getTargetUser).toList());
        userIds.addAll(page.map(Log::getStaffId).toList());

        profileRepository.findManyByIdIn(new ArrayList<>(userIds)).forEach(profile -> {
            usernames.put(profile.getId(), profile.getUsername());
        });

        page.forEach(log -> {
            JsonObject logObject = new JsonObject();

            logObject.addProperty("id", log.getLogId());
            logObject.addProperty("logTypeId", log.getLogTypeId());
            logObject.addProperty("target", usernames.getOrDefault(log.getTargetUser(), "None"));
            logObject.addProperty("staff", usernames.getOrDefault(log.getStaffId(), "None"));
            logObject.addProperty("description", log.getDescription());

            logsArray.add(logObject);
        });

        object.addProperty("success", true);
        object.add("logs", logsArray);

        return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(200));
    }

    @PutMapping(
            value = "/admin/disable-mfa",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> disableMfa(
            @RequestParam int userId
    ) {
        JsonObject object = new JsonObject();

        UserDetailsEntity userDetails = (UserDetailsEntity) SecurityContextHolder.getContext().getAuthentication();
        Optional<Profile> targetProfileOptional = profileRepository.findById(userId);

        if (targetProfileOptional.isEmpty()) {
            object.addProperty("error", true);
            object.addProperty("error_code", "no_user_found");

            return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(400));
        } else if (!targetProfileOptional.get().getMfaEnabled()) {
            object.addProperty("error", true);
            object.addProperty("error_code", "mfa_not_enabled");

            return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(400));
        }

        Profile targetProfile = targetProfileOptional.get();

        if (!rankCheckService.isUserGroupHigher(userDetails.getProfile(), targetProfile)) {
            object.addProperty("error", true);
            object.addProperty("error_code", "target_group_too_high");

            return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(403));
        }

        Log log = new Log();

        log.setLogTypeId(LogType.DISABLE_MFA.getType());
        log.setStaffId(userDetails.getProfile().getId());
        log.setTargetUser(targetProfile.getId());
        log.setDescription("Disabled MFA");

        targetProfile.setMfaEnabled(false);
        targetProfile.setMfaSecret(null);

        logsRepository.saveAndFlush(log);
        profileRepository.saveAndFlush(targetProfile);

        object.addProperty("success", true);

        return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(200));
    }

    @PutMapping(
            value = "/admin/update-user-group",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> handleUserGroupUpdate(@RequestBody UpdateGroupSchema schema) {
        JsonObject object = new JsonObject();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsEntity userDetails = (UserDetailsEntity) authentication.getPrincipal();

        Profile profile = userDetails.getProfile();
        List<Rank> groups = userDetails.getGroups();

        Optional<Rank> targetOptional = rankRepository.findById(schema.getGroupId());

        if (targetOptional.isEmpty()) {
            object.addProperty("error", true);
            object.addProperty("error_code", "target_group_not_found");

            return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(400));
        }

        Rank targetGroup = targetOptional.get();

        if (groups.stream().noneMatch(group -> group.getPriority() > targetGroup.getPriority())) {
            object.addProperty("error", true);
            object.addProperty("error_code", "target_group_too_high");

            return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(403));
        }

        UserGroupId userGroupId = new UserGroupId();

        userGroupId.setGroupId(targetGroup.getId());
        userGroupId.setUserId(schema.getUserSelection());

        Log log = new Log();

        log.setLogTypeId(LogType.UPDATE_USER_GROUP.getType());
        log.setStaffId(profile.getId());
        log.setTargetUser(schema.getUserSelection());
        log.setDescription(String.format((schema.isAdd() ? "Added group %s to user" : "Removed group %s"), targetGroup.getName()));

        List<UserGroupId> currentGroups = groupsRepository.findManyByUserGroupIdUserId(schema.getUserSelection())
                .stream()
                .map(UserGroup::getUserGroupId)
                .toList();

        if (schema.isAdd()) {
            if (currentGroups.stream().anyMatch(profileGroup -> profileGroup.getGroupId() == schema.getGroupId())) {
                object.addProperty("error", true);
                object.addProperty("error_code", "already_in_group");

                return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(400));
            }

            UserGroup userGroup = new UserGroup();
            userGroup.setUserGroupId(userGroupId);

            groupsRepository.saveAndFlush(userGroup);

            object.addProperty("action", "add_user_to_group");
        } else {
            if (currentGroups.stream().noneMatch(profileGroup -> profileGroup.getGroupId() == schema.getGroupId())) {
                object.addProperty("error", true);
                object.addProperty("error_code", "not_in_group");

                return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(400));
            }

            groupsRepository.deleteById(userGroupId);

            object.addProperty("action", "remove_user_from_group");
        }

        logsRepository.saveAndFlush(log);

        object.addProperty("success", true);

        return new ResponseEntity<>(gson.toJson(targetGroup), HttpStatusCode.valueOf(200));
    }

    @DeleteMapping(
            name = "/admin/delete-user",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> handleDelete(
            @RequestParam int id
    ) {
        JsonObject object = new JsonObject();

        if (profileRepository.findById(id).isEmpty()) {
            object.addProperty("error", true);
            object.addProperty("error_code", "user_not_found");

            return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(406));
        }

        profileRepository.deleteById(id);

        return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(200));
    }
}
