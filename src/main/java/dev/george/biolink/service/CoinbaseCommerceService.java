package dev.george.biolink.service;

import com.google.common.io.BaseEncoding;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.george.biolink.entity.ChargeEntity;
import dev.george.biolink.model.Discount;
import dev.george.biolink.model.PaymentPackage;
import dev.george.biolink.model.Profile;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

@Service
public class CoinbaseCommerceService {

    private static final String COINBASE_API_VERSION = "2018-03-22";
    private static final String COINBASE_API_ROOT = "https://api.commerce.coinbase.com/";

    private static final String SIGNING_ALGORITHM = "HmacSHA256";
    private static final Charset UTF8 = StandardCharsets.UTF_8;

    private final Mac sha256Hmac;
    private final BaseEncoding signatureEncoding;

    private final Gson gson;

    @Value("${coinbase.commerce.key}")
    private String coinbaseCommerceKey;

    @Value("${coinbase.commerce.webhook.secret}")
    private String coinbaseCommerceWebhookSecret;

    public CoinbaseCommerceService(Gson gson) throws NoSuchAlgorithmException, InvalidKeyException {
        this.gson = gson;

        SecretKeySpec secretKey = new SecretKeySpec(coinbaseCommerceWebhookSecret.getBytes(UTF8), SIGNING_ALGORITHM);

        this.sha256Hmac = Mac.getInstance(SIGNING_ALGORITHM);
        sha256Hmac.init(secretKey);

        this.signatureEncoding = BaseEncoding.base16().lowerCase();
    }

    public Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();

        headers.put("Content-Type", "application/json");
        headers.put("X-CC-Api-Key", coinbaseCommerceKey);
        headers.put("X-CC-Version", COINBASE_API_VERSION);
        headers.put("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Mobile Safari/537.36");

        return headers;
    }

    public URL getPath(String path) throws IOException {
        return new URL(COINBASE_API_ROOT + path);
    }

    public ChargeEntity createCharge(
            @NotNull Profile profile,
            @NotNull PaymentPackage paymentPackage,
            @Nullable Discount discount
    ) {
        double amount = paymentPackage.getPrice();

        if (discount != null) {
            amount -= paymentPackage.getPrice() * discount.getDiscountAmount();
        }

        ChargeEntity charge = new ChargeEntity();

        charge.setName(paymentPackage.getName());
        charge.setDescription(paymentPackage.getDescription());
        charge.setAmount(amount);

        Map<String, Object> metadata = new HashMap<>();

        metadata.put("packageId", paymentPackage.getId());
        metadata.put("profileId", profile.getId());

        metadata.forEach(charge.getMetadata()::put);

        return charge;
    }

    public String create(ChargeEntity entity) throws IOException {
        URL url = getPath("charges");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        JsonObject postData = new JsonObject();

        postData.addProperty("name", entity.getName());
        postData.addProperty("description", entity.getDescription());

        JsonObject localPrice = new JsonObject();

        localPrice.addProperty("amount", entity.getAmount());
        localPrice.addProperty("currency", "USD");

        postData.add("local_price", localPrice);
        postData.addProperty("pricing_type", "fixed_price");

        JsonObject metadata = new JsonObject();

        entity.getMetadata().forEach((key, value) -> {
            if (value instanceof String) {
                metadata.addProperty(key, (String) value);
            } else if (value instanceof Number) {
                metadata.addProperty(key, (Number) value);
            }
        });

        postData.add("metadata", metadata);

        getHeaders().forEach(connection::setRequestProperty);

        byte[] bytes = this.gson.toJson(postData).getBytes();

        connection.setFixedLengthStreamingMode(bytes.length);
        connection.setDoOutput(true);

        connection.connect();
        connection.getOutputStream().write(bytes);

        InputStream inputStream = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        StringBuilder builder = new StringBuilder();

        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }

        JsonObject response = this.gson.fromJson(builder.toString(), JsonObject.class);

        return response.get("data").getAsJsonObject().get("hosted_url").getAsString();
    }

    public boolean isValidSignature(String signature, String message) {
        return signatureEncoding.encode(
                sha256Hmac.doFinal((message == null ? "".getBytes() : message.getBytes(UTF8)))
        ).equals(signature);
    }
}
