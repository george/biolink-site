package dev.george.biolink.schema.staff;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ReassignRedirectSchema {

    private String redirect;
    private int newProfileId;

}
