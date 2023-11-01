package dev.george.biolink.service;

import dev.george.biolink.model.Context;
import dev.george.biolink.model.Profile;
import dev.george.biolink.repository.ContextRepository;
import dev.george.biolink.repository.ProfileIpsRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.Map;

@AllArgsConstructor
@Service
public class MfaService {

    private final ContextRepository contextRepository;
    private final ProfileIpsRepository profileIpsRepository;
    private final JwtService jwtService;

    public boolean requiresMfaOnProfile(Profile profile, HttpServletRequest request) {
        boolean requiresMfa = profile.getMfaEnabled() != null && profile.getMfaEnabled();

        if (!requiresMfa) {
            String hashedCurrentIp = BCrypt.hashpw(request.getRemoteAddr(), profile.getIpSalt());

            if (profileIpsRepository.findAllByProfileIpIdProfileId(profile.getId()).stream()
                    .noneMatch((profileIp) -> profileIp.getProfileIpId().getIpAddress().equals(hashedCurrentIp))) {
                requiresMfa = true;
            }
        }

        return requiresMfa;
    }

    public boolean verifyMfa(String response, String contextToken, Profile profile) {
        String authenticationType = (String) jwtService.getClaimFromToken(contextToken,
                (claims) -> claims.get("requiredMfaType"));

        switch (authenticationType) {
            case "email":
                break;
            case "totp": {
                break;
            }
        }

        return true;
    }

    public String createJwtContext(Map<String, Object> claims, int profileId) {
        Context context = new Context();
        String jwtToken = jwtService.generateToken(claims, Integer.toString(profileId));

        context.setUserId(profileId);
        context.setContextMeta(jwtToken);

        contextRepository.save(context);

        return jwtToken;
    }

    public String getVerificationMethod(Profile profile) {
        return profile.getMfaEnabled() ? "totp" : "email";
    }
}
