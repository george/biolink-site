package dev.george.biolink.response;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.george.biolink.model.Profile;
import dev.george.biolink.service.JwtService;
import dev.george.biolink.service.MfaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;

@Component
public class AuthenticationResponses {

    private final Gson gson;
    private final JwtService jwtService;
    private final MfaService mfaService;

    @Value("${biolink.cookie.domain}")
    private String cookieDomain;

    public AuthenticationResponses(Gson gson, JwtService jwtService, MfaService mfaService) {
        this.gson = gson;
        this.jwtService = jwtService;
        this.mfaService = mfaService;
    }

    public ResponseEntity<String> getInvalidEmailOrPassword() {
        JsonObject object = new JsonObject();

        object.addProperty("error", true);
        object.addProperty("invalid_email_or_password", true);

        return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(400));
    }

    public ResponseEntity<String> getPasswordLengthResponse() {
        JsonObject object = new JsonObject();

        object.addProperty("error", true);
        object.addProperty("error_code", "password_length");

        return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(400));
    }

    public ResponseEntity<String> getReCaptchaResponseFailed() {
        JsonObject object = new JsonObject();

        object.addProperty("error", true);
        object.addProperty("error_code", "recaptcha_response_failed");

        return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(400));
    }

    public ResponseEntity<String> completeAuthentication(Profile profile, boolean verificationCompleted) {
        JsonObject object = new JsonObject();

        if (!verificationCompleted) {
            object.addProperty("error", true);
            object.addProperty("error_code", "invalid_mfa_code");

            return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(403));
        }

        object.addProperty("success", true);

        return transformProfileAndDataToResponse(object, profile);
    }

    public ResponseEntity<String> additionalMfaRequired(Profile profile, String ipAddress) {
        JsonObject object = new JsonObject();
        Map<String, Object> claims = new HashMap<>();

        claims.put("ipAddress", ipAddress);
        claims.put("requiredMfaType", mfaService.getVerificationMethod(profile));

        String jwtToken = mfaService.createJwtContext(claims, profile.getId());

        object.addProperty("error", true);
        object.addProperty("error_code", "additional_mfa_required");
        object.addProperty("required_mfa_type", mfaService.getVerificationMethod(profile));
        object.addProperty("context_token", jwtToken);

        return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(401));
    }

    public ResponseEntity<String> invalidContextId() {
        JsonObject object = new JsonObject();

        object.addProperty("error", true);
        object.addProperty("error_code", "invalid_context_id");

        return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(400));
    }

    public ResponseEntity<String> getIllegalEmail() {
        JsonObject object = new JsonObject();

        object.addProperty("error", true);
        object.addProperty("error_code", "invalid_email");

        return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(400));
    }

    public ResponseEntity<String> getKeyInUse(String key) {
        JsonObject object = new JsonObject();

        object.addProperty("error", true);
        object.addProperty("error_code", key + "_in_use");

        return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(400));
    }

    public ResponseEntity<String> getProfileCreationCompleted(Profile profile) {
        JsonObject object = new JsonObject();

        object.addProperty("success", true);

        return transformProfileAndDataToResponse(object, profile);
    }

    public ResponseEntity<String> transformProfileAndDataToResponse(JsonObject object, Profile profile) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        Map<String, Object> claims = new HashMap<>();

        claims.put("id", profile.getId());
        claims.put("ip", profile.getLastIpString());

        headers.add("Set-Cookie", String.format("session=%s; SameSite=Strict; Path=/; Domain=%s; Secure", jwtService.generateToken(claims,
                Integer.toString(profile.getId())), cookieDomain));

        return new ResponseEntity<>(gson.toJson(object), headers, HttpStatusCode.valueOf(200));
    }
}
