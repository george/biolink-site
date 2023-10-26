package dev.george.biolink.schema;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

@Getter @Setter
public class LoginSchema {

    private String email;
    private String password;
    private String captchaResponseKey;

    @Nullable private String authenticationPurpose;

}
