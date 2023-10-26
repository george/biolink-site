package dev.george.biolink.controller;

import dev.george.biolink.repository.ProfileRepository;
import dev.george.biolink.model.Profile;
import dev.george.biolink.response.AuthenticationResponses;
import dev.george.biolink.schema.LoginSchema;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@AllArgsConstructor
@RestController
public class AuthenticationController {

    private final AuthenticationResponses responses;
    private final PasswordEncoder encoder;
    private final ProfileRepository repository;

    @PostMapping(
            value = "/test",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> test(@RequestBody LoginSchema schema) {
        return new ResponseEntity<>("", HttpStatusCode.valueOf(200));
    }

    @PostMapping(
            value = "/auth/login",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> getData(@RequestBody LoginSchema schema) {
        if (schema.getPassword() == null || schema.getPassword().length() < 8 || schema.getPassword().length() > 128) {
            return responses.getPasswordLengthResponse();
        }

        if (schema.getCaptchaResponseKey() == null || schema.getCaptchaResponseKey().length() < 8) {
            return responses.getReCaptchaResponseFailed();
        }

        Optional<Profile> optional = repository.findOneByEmail(schema.getEmail());

        if (optional.isEmpty()) {
            return responses.getInvalidEmailOrPassword();
        }

        Profile profile = optional.get();

//        if (!encoder.matches(schema.getPassword(), profile.getPassword())) {
//            return responses.getInvalidEmailOrPassword();
//        }

        return responses.completeAuthentication(profile);
    }
}
