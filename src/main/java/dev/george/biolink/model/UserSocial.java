package dev.george.biolink.model;


import dev.george.biolink.entity.UserSocialUserPlatformUsername;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_social", schema = "public")
@Getter @Setter
public class UserSocial {

    @EmbeddedId private UserSocialUserPlatformUsername key;

}
