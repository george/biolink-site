package dev.george.biolink.schema.staff;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class GetUserProfileSchema {

    private int userId;

    private String username;
    private String email;
    private String lastIp;

}
