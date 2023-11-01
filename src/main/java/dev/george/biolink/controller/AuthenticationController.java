package dev.george.biolink.controller;

import dev.george.biolink.entity.ProfileIpId;
import dev.george.biolink.model.ProfileIp;
import dev.george.biolink.repository.ProfileIpsRepository;
import dev.george.biolink.repository.ProfileRepository;
import dev.george.biolink.model.Profile;
import dev.george.biolink.response.AuthenticationResponses;
import dev.george.biolink.schema.auth.LoginSchema;
import dev.george.biolink.schema.auth.RegisterSchema;
import dev.george.biolink.service.JwtService;
import dev.george.biolink.service.MfaService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

@AllArgsConstructor
@RestController
public class AuthenticationController {

    private final AuthenticationResponses responses;
    private final JwtService jwtService;
    private final MfaService mfaService;
    private final PasswordEncoder encoder;
    private final ProfileRepository profileRepository;
    private final ProfileIpsRepository profileIpsRepository;

    @PostMapping(
            value = "/auth/login",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> getData(@RequestBody LoginSchema schema, HttpServletRequest request) {
        if (schema.getContextToken() == null) {
            if (schema.getPassword() == null || schema.getPassword().length() < 8 || schema.getPassword().length() > 128) {
                return responses.getPasswordLengthResponse();
            }

            if (schema.getCaptchaResponseKey() == null || schema.getCaptchaResponseKey().length() < 8) {
                return responses.getReCaptchaResponseFailed();
            }

            Optional<Profile> optional = profileRepository.findOneByEmail(schema.getEmail());

            if (optional.isEmpty()) {
                return responses.getInvalidEmailOrPassword();
            }

            Profile profile = optional.get();

            if (!encoder.matches(schema.getPassword(), profile.getPassword())) {
                return responses.getInvalidEmailOrPassword();
            }

            if (!mfaService.requiresMfaOnProfile(profile, request)) {
                profile.setLastLogin(Timestamp.from(Instant.now()));
                profile.setLastIp(request.getRemoteAddr());

                profileRepository.saveAndFlush(profile);

                return responses.completeAuthentication(profile, true);
            }

            return responses.additionalMfaRequired(profile, request.getRemoteAddr());
        }

        try {
            if (Date.from(Instant.now()).toInstant().isAfter(
                    jwtService.getExpirationDateFromToken(schema.getContextToken()).toInstant())) {
                return responses.invalidContextId();
            }

            Optional<Profile> optionalProfile = profileRepository.findById(
                    jwtService.getIdFromToken(schema.getContextToken()));

            if (optionalProfile.isEmpty()) {
                return responses.invalidContextId();
            }

            Profile profile = optionalProfile.get();

            return responses.completeAuthentication(profile, mfaService.verifyMfa(
                    schema.getVerificationCode(), schema.getContextToken(), profile));
        } catch (Exception exc) {
            return responses.invalidContextId();
        }
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

        if (profileRepository.findOneByUsername(schema.getUsername()).isPresent()) {
            return responses.getKeyInUse("username");
        }

        if (profileRepository.findOneByEmail(schema.getEmail()).isPresent()) {
            return responses.getKeyInUse("email");
        }

        Profile profile = new Profile();

        profile.setEmail(schema.getEmail());
        profile.setUsername(schema.getUsername());
        profile.setPassword(encoder.encode(schema.getPassword()));
        profile.setCreatedAt(Timestamp.from(Instant.now()));
        profile.setLastLogin(Timestamp.from(Instant.now()));
        profile.setLastIp(request.getRemoteAddr());
        profile.setIpSalt(BCrypt.gensalt(4));

        profileRepository.saveAndFlush(profile);

        profileRepository.findOneByEmail(profile.getEmail()).ifPresent((userProfile) -> {
            ProfileIpId profileIpId = new ProfileIpId();

            profileIpId.setProfileId(userProfile.getId());
            profileIpId.setIpAddress(BCrypt.hashpw(userProfile.getLastIpString(), profile.getIpSalt()));

            ProfileIp profileIp = new ProfileIp();
            profileIp.setProfileIpId(profileIpId);

            profileIpsRepository.saveAndFlush(profileIp);
        });

        return responses.getProfileCreationCompleted(profile);
    }
}
