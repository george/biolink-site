package dev.george.biolink.schema.auth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

@Getter @Setter
public class LoginSchema {

    private String email;
    private String password;
    private String captchaResponseKey;

    private String contextId;
    private String verificationCode;

    @Nullable private String authenticationPurpose;

}
