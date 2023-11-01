package dev.george.biolink.schema.staff;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ChangeUserUsernameSchema {

    private int userId;

    private String currentUsername;
    private String newUsername;

}
