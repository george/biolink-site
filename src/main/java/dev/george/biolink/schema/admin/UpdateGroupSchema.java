package dev.george.biolink.schema.admin;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateGroupSchema {

    private int userSelection;
    private int groupId;

    private boolean add;

}
