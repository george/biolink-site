package dev.george.biolink.schema.admin;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateGroupSchema {

    private Integer userSelection;
    private Integer groupId;

    private boolean add;

}
