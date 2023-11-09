package dev.george.biolink.service;

import com.google.gson.JsonObject;
import dev.george.biolink.entity.UserGroupId;
import dev.george.biolink.model.PaymentPackage;
import dev.george.biolink.model.Profile;
import dev.george.biolink.model.UserGroup;
import dev.george.biolink.repository.PaymentPackageRepository;
import dev.george.biolink.repository.ProfileRepository;
import dev.george.biolink.repository.UserGroupsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@AllArgsConstructor
@Service
public class PaymentService {

    private final PaymentPackageRepository paymentPackageRepository;
    private final ProfileRepository profileRepository;
    private final UserGroupsRepository userGroupsRepository;

    public void handleCompletion(JsonObject metadata) {
        int packageId = metadata.get("packageId").getAsInt();
        int profileId = metadata.get("profileId").getAsInt();

        Optional<PaymentPackage> paymentPackageOptional = paymentPackageRepository.findById(packageId);
        Optional<Profile> profileOptional = profileRepository.findById(profileId);

        if (paymentPackageOptional.isEmpty() || profileOptional.isEmpty()) {
            return;
        }

        PaymentPackage paymentPackage = paymentPackageOptional.get();
        UserGroupId key = new UserGroupId();

        key.setUserId(profileId);
        key.setGroupId(paymentPackage.getId());

        UserGroup userGroup = new UserGroup();
        userGroup.setUserGroupId(key);

        userGroupsRepository.saveAndFlush(userGroup);
    }

}
