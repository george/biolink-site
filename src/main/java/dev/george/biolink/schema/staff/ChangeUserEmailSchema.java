package dev.george.biolink.schema.staff;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ChangeUserEmailSchema {

    private int userId;

    private String oldEmail;
    private String newEmail;

}
