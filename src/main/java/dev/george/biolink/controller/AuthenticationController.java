package dev.george.biolink.controller;

import dev.george.biolink.repository.ProfileRepository;
import dev.george.biolink.model.Profile;
import dev.george.biolink.response.AuthenticationResponses;
import dev.george.biolink.schema.LoginSchema;
import dev.george.biolink.schema.RegisterSchema;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

@AllArgsConstructor
@RestController
public class AuthenticationController {

    private final AuthenticationResponses responses;
    private final PasswordEncoder encoder;
    private final ProfileRepository repository;

    @PostMapping(
            value = "/auth/login",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> getData(@RequestBody LoginSchema schema, HttpServletRequest request) {
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

        if (!encoder.matches(schema.getPassword(), profile.getPassword())) {
            return responses.getInvalidEmailOrPassword();
        }

        profile.setLastLogin(Timestamp.from(Instant.now()));
        profile.setLastIp(request.getRemoteAddr());

        repository.saveAndFlush(profile);

        return responses.completeAuthentication(profile);
    }

    @PostMapping(
            value = "/auth/register",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> getData(@RequestBody RegisterSchema schema, HttpServletRequest request) {
        if (schema.getCaptchaResponseKey() == null || schema.getCaptchaResponseKey().length() < 8) {
            return responses.getReCaptchaResponseFailed();
        }

        if (schema.getPassword() == null || schema.getPassword().length() < 8 || schema.getPassword().length() > 128) {
            return responses.getPasswordLengthResponse();
        }

        if (schema.getEmail() == null || !schema.getEmail().contains(".") || !schema.getEmail().contains("@")
                || schema.getEmail().split("@").length != 2 || !schema.getEmail().split("@")[1].contains(".")) {
            return responses.getIllegalEmail();
        }

        if (repository.findOneByUsername(schema.getUsername()).isPresent()) {
            return responses.getKeyInUse("username");
        }

        if (repository.findOneByEmail(schema.getEmail()).isPresent()) {
            return responses.getKeyInUse("email");
        }

        Profile profile = new Profile();

        profile.setEmail(schema.getEmail());
        profile.setUsername(schema.getUsername());
        profile.setPassword(encoder.encode(schema.getPassword()));
        profile.setCreatedAt(Timestamp.from(Instant.now()));
        profile.setLastLogin(Timestamp.from(Instant.now()));
        profile.setLastIp(request.getRemoteAddr());

        repository.saveAndFlush(profile);

        return responses.getProfileCreationCompleted(profile);
    }
}
