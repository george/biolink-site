package dev.george.biolink.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.george.biolink.entity.ChargeEntity;
import dev.george.biolink.entity.UserDetailsEntity;
import dev.george.biolink.model.Discount;
import dev.george.biolink.model.PaymentPackage;
import dev.george.biolink.repository.DiscountRepository;
import dev.george.biolink.repository.PaymentPackageRepository;
import dev.george.biolink.schema.payment.ChargeSchema;
import dev.george.biolink.service.CoinbaseCommerceService;
import dev.george.biolink.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@AllArgsConstructor
@RestController
public class CoinbaseCommercePaymentController {

    private final DiscountRepository discountRepository;
    private final CoinbaseCommerceService coinbaseCommerceService;
    private final Gson gson;
    private final PaymentPackageRepository paymentPackageRepository;
    private final PaymentService paymentService;

    @PostMapping(
            value = "/create-coinbase-commerce-charge",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> createCharge(
            @RequestBody ChargeSchema schema
    ) {
        JsonObject object = new JsonObject();
        ResponseEntity<String> error = paymentService.handleChargeRequest(schema);

        if (error != null) {
            return error;
        }

        UserDetailsEntity entity = (UserDetailsEntity) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        Optional<PaymentPackage> paymentPackageOptional = paymentPackageRepository.findById(schema.getPackageId());
        Optional<Discount> discountOptional = discountRepository.findDiscountByPromotionCode(schema.getDiscountCode());

        try {
            ChargeEntity charge = coinbaseCommerceService.createCharge(
                    entity.getProfile(),
                    paymentPackageOptional.get(),
                    discountOptional.get());

            String url = coinbaseCommerceService.create(charge);

            object.addProperty("success", true);
            object.addProperty("url", url);

            return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(200));
        } catch (Exception exc) {
            exc.printStackTrace();

            object.addProperty("error", true);
            object.addProperty("error_code", "payment_failed");

            return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(400));
        }
    }

    @PostMapping(
            value = "/webhook/coinbase-commerce-handler",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> handleCoinbaseCommerceWebhook(
            @RequestBody String postData,
            HttpServletRequest request
    ) {
        JsonObject object = new JsonObject();
        JsonObject postObject;

        try {
            postObject = gson.fromJson(postData, JsonObject.class);
        } catch (Exception exc) {
            object.addProperty("error", true);
            object.addProperty("error_code", "failed_to_parse_json");

            return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(400));
        }

        JsonObject eventObject = postObject.get("event").getAsJsonObject();

        if (!eventObject.get("type").getAsString().equals("charge:confirmed") ||
                request.getHeader("X-CC-Webhook-Signature") == null) {
            object.addProperty("success", true);

            return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(204));
        }

        String signature = request.getHeader("X-CC-Webhook-Signature");

        if (!coinbaseCommerceService.isValidSignature(signature, postData)) {
            object.addProperty("error", true);
            object.addProperty("error_code", "invalid_signature");

            return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(403));
        }

        JsonObject metadata = eventObject.get("metadata").getAsJsonObject();
        paymentService.handleCompletion(metadata);

        object.addProperty("success", true);

        return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(200));
    }
}
