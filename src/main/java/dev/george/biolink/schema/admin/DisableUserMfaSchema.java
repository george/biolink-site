package dev.george.biolink.schema.admin;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DisableUserMfaSchema {

    private int userId;

    private String username;
    private String email;

}
