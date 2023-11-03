package dev.george.biolink.model;

import dev.george.biolink.entity.InviteUserIdCode;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "invite", schema = "public")
@Getter @Setter
public class Invite {

    @EmbeddedId private InviteUserIdCode key;

}
