package dev.george.biolink.model.impl;

import dev.george.biolink.model.Model;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class User implements Model {

    @Id
    private long id;

    private String username;
    private String password;
    private String email;

    private int lastIp;

    private long createdAt;
    private long lastLogin;

    private String invitedBy;

}
