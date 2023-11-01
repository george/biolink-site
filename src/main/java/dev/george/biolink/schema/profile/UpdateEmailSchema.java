package dev.george.biolink.schema.profile;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateEmailSchema {

    private String newEmail;

    private String contextToken;
    private String verificationCode;
}
