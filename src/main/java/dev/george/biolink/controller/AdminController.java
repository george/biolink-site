package dev.george.biolink.controller;

import com.google.gson.Gson;
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
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
public class AdminController {

    private final LogsRepository logsRepository;
    private final RankRepository rankRepository;
    private final ProfileRepository profileRepository;
    private final UserGroupsRepository groupsRepository;

    private final Gson gson;

    @PutMapping(
            value = "/admin/add-rank",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> handleUserGroupUpdate(UpdateGroupSchema schema) {
        JsonObject object = new JsonObject();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsEntity userDetails = (UserDetailsEntity) authentication.getDetails();

        Profile profile = userDetails.getProfile();
        List<Rank> groups = userDetails.getGroups();

        System.out.println(groups.size());
        groups.forEach(System.out::println);

        Rank targetGroup = rankRepository.findById(schema.getGroupId());

        if (groups.stream().noneMatch(group -> group.getPriority() > targetGroup.getPriority())) {
            object.addProperty("error", true);
            object.addProperty("error_code", "target_rank_too_high");

            return new ResponseEntity<>(gson.toJson(targetGroup), HttpStatusCode.valueOf(403));
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
