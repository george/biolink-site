package dev.george.biolink.model;

import dev.george.biolink.entity.UserGroupId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_group", schema = "public")
@Getter @Setter
public class UserGroup {

    @EmbeddedId private UserGroupId userGroupId;

}
