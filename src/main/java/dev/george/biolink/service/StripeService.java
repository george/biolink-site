package dev.george.biolink.service;

import com.google.gson.JsonObject;
import com.stripe.Stripe;
import com.stripe.exception.*;
import com.stripe.model.Charge;
import dev.george.biolink.model.Discount;
import dev.george.biolink.model.PaymentPackage;
import dev.george.biolink.model.Profile;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StripeService {

    @Value("${stripe.key.private}")
    private String privateKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = privateKey;
    }

    public Charge createCharge(
            @NotNull Profile profile,
            @NotNull PaymentPackage paymentPackage,
            @Nullable Discount discount
    ) throws StripeException {
        double amount = paymentPackage.getPrice();

        if (discount != null) {
            amount -= paymentPackage.getPrice() * discount.getDiscountAmount();
        }

        Map<String, Object> chargeData = new HashMap<>();
        Map<String, Object> metadata = new HashMap<>();

        chargeData.put("amount", amount);
        chargeData.put("currency", "USD");
        chargeData.put("description", paymentPackage.getDescription());

        metadata.put("packageId", paymentPackage.getId());
        metadata.put("profileId", profile.getId());
        metadata.put("source", "tok_visa");

        chargeData.put("metadata", metadata);

        return Charge.create(chargeData);
    }
}
