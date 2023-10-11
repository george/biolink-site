package dev.george.biolink.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenticationController {

    @Autowired
    private Gson gson;

    @PostMapping(value = "/login", produces = "application/json")
    public ResponseEntity<String> getData(
            @RequestParam(value = "identifier") String identifier,
            @RequestParam(value = "password") String password,
            @RequestParam(value = "g-recaptcha-response-key") String responseKey,
            @Nullable @RequestParam(value = "authentication-purpose") String authenticationPurpose) {
        String query = identifier.contains("@") ? "email" : "username";

        if (password.length() < 8) {
            JsonObject object = new JsonObject();

            object.addProperty("error", true);
            object.addProperty("error_code", "password_length");

            return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(400));
        }

        return null;
    }

    private ResponseEntity<String> failForInvalidIdentifierOrPassword() {
        JsonObject object = new JsonObject();

        object.addProperty("error", true);
        object.addProperty("invalid_identifier_or_password", true);

        return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(400));
    }
}
