package dev.george.biolink.model;

import dev.george.biolink.entity.ProfileComponentUserIdComponentIndex;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "profile_component", schema = "public")
@Getter @Setter
public class ProfileComponent {

    @EmbeddedId private ProfileComponentUserIdComponentIndex key;

    private int componentId;
    private int componentIndex;

}
