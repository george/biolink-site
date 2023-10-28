package dev.george.biolink.controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.george.biolink.model.Profile;
import dev.george.biolink.model.Rank;
import dev.george.biolink.repository.BansRepository;
import dev.george.biolink.repository.ProfileRepository;
import dev.george.biolink.repository.RankRepository;
import dev.george.biolink.repository.UserGroupsRepository;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@RestController
public class StaffController {

    private final BansRepository bansRepository;
    private final Gson gson;
    private final ProfileRepository profileRepository;
    private final RankRepository rankRepository;
    private final UserGroupsRepository groupsRepository;

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
        List<Integer> groupIds = groupsRepository.findManyByUserGroupIdUserId(userId)
                .stream()
                .map(group -> group.getUserGroupId().getGroupId())
                .toList();

        List<Rank> groups = rankRepository.findAllByIdIn(groupIds);

        JsonArray array = new JsonArray();

        groups.forEach(group -> {
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
            profileObject.addProperty("email", profile.getEmail());
            profileObject.addProperty("lastIp", profile.getLastIpString());
            profileObject.addProperty("createdAt", profile.getCreatedAt().getTime());
            profileObject.addProperty("lastLogin", profile.getLastLogin().getTime());

            users.add(profileObject);
        });

        return new ResponseEntity<>(gson.toJson(users), HttpStatusCode.valueOf(200));
    }
}
