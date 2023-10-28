package dev.george.biolink.schema.auth;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegisterSchema {

    private String email;
    private String username;
    private String password;
    private String captchaResponseKey;

    private String inviteCode;

}
