package dev.george.biolink.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter @Setter
public class InviteUserIdCode implements Serializable {

    private int userId;
    private String inviteCode;

}
