package dev.george.biolink.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import dev.george.biolink.entity.UserDetailsEntity;
import dev.george.biolink.model.Discount;
import dev.george.biolink.model.PaymentPackage;
import dev.george.biolink.repository.*;
import dev.george.biolink.schema.payment.ChargeSchema;
import dev.george.biolink.service.PaymentService;
import dev.george.biolink.service.StripeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class StripePaymentController {

    private final DiscountRepository discountRepository;
    private final Gson gson;
    private final PaymentPackageRepository paymentPackageRepository;
    private final PaymentService paymentService;
    private final StripeService stripeService;

    @Value("${stripe.endpoint.secret}")
    private String endpointSecret;

    public StripePaymentController(DiscountRepository discountRepository, Gson gson, PaymentPackageRepository paymentPackageRepository,
                                   PaymentService paymentService, StripeService stripeService) {
        this.discountRepository = discountRepository;
        this.gson = gson;
        this.paymentPackageRepository = paymentPackageRepository;
        this.paymentService = paymentService;
        this.stripeService = stripeService;
    }

    @PostMapping(
            value = "/create-stripe-charge",
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
            Charge charge = stripeService.createCharge(
                    entity.getProfile(),
                    paymentPackageOptional.get(),
                    discountOptional.get());

            object.addProperty("id", charge.getId());
            object.addProperty("status", charge.getStatus());
            object.addProperty("chargeId", charge.getId());
            object.addProperty("balanceTransactionId", charge.getBalanceTransaction());

            return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(200));
        } catch (Exception exc) {
            exc.printStackTrace();

            object.addProperty("error", true);
            object.addProperty("error_code", "payment_failed");

            return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(400));
        }
    }

    @PostMapping(
            value = "/webhook/stripe-handler",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String postData,
            HttpServletRequest request
    ) {
        JsonObject object = new JsonObject();

        String signature = request.getHeader("Stripe-Signature");
        Event event;

        try {
            event = Webhook.constructEvent(postData, signature, endpointSecret);
        } catch (StripeException exc) {
            exc.printStackTrace();

            object.addProperty("error", true);
            object.addProperty("error_code", "payment_processing_error");

            return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(403));
        }

        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject;

        if (dataObjectDeserializer.getObject().isEmpty()) {
            object.addProperty("error", true);
            object.addProperty("error_code", "object_data_invalid");

            return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(400));
        } else {
            stripeObject = dataObjectDeserializer.getObject().get();
        }

        if (!event.getType().equals("payment_intent.succeeded")) {
            object.addProperty("error", true);
            object.addProperty("error_code", "unknown_payment_intent");

            return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(400));
        }

        JsonObject metadata = gson.fromJson(stripeObject.toJson(), JsonObject.class).get("metadata").getAsJsonObject();

        if (!metadata.has("packageId") || !metadata.has("profileId")) {
            object.addProperty("error", true);
            object.addProperty("error_code", "missing_data");

            return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(400));
        }

        paymentService.handleCompletion(metadata);

        object.addProperty("success", true);

        return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(200));
    }
}
