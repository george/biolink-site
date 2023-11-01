package dev.george.biolink.schema.staff;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BanUserSchema {

    private int userId;

    private String reason;

}
