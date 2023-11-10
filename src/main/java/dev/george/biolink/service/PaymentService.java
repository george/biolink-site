package dev.george.biolink.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.george.biolink.entity.UserGroupId;
import dev.george.biolink.model.Discount;
import dev.george.biolink.model.PaymentPackage;
import dev.george.biolink.model.Profile;
import dev.george.biolink.model.UserGroup;
import dev.george.biolink.repository.DiscountRepository;
import dev.george.biolink.repository.PaymentPackageRepository;
import dev.george.biolink.repository.ProfileRepository;
import dev.george.biolink.repository.UserGroupsRepository;
import dev.george.biolink.schema.payment.ChargeSchema;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Optional;

@AllArgsConstructor
@Service
public class PaymentService {

    private final DiscountRepository discountRepository;
    private final Gson gson;
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

    public ResponseEntity<String> handleChargeRequest(ChargeSchema schema) {
        JsonObject object = new JsonObject();

        Optional<PaymentPackage> paymentPackageOptional = paymentPackageRepository.findById(schema.getPackageId());
        Optional<Discount> discountOptional = discountRepository.findDiscountByPromotionCode(schema.getDiscountCode());

        if (paymentPackageOptional.isEmpty()) {
            object.addProperty("error", true);
            object.addProperty("error_code", "invalid_payment_package");

            return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(400));
        }

        if (discountOptional.isPresent()) {
            Discount discount = discountOptional.get();
            Timestamp time = new Timestamp(System.currentTimeMillis());

            if (discount.getAvailableFrom().before(time) && discount.getAvailableTo().after(time)) {
                object.addProperty("error", true);
                object.addProperty("error_code", "discount_invalid_or_expired");

                return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(400));
            }
        }

        return null;
    }
}
