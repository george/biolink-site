package dev.george.biolink.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter @Setter
@Embeddable
public class UserGroupId implements Serializable {

    private int userId;
    private int groupId;

}
