package dev.george.biolink.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "profile_redirect", schema = "public")
@Getter @Setter
public class Redirect {

    @Id @NotNull private int userId;
    @NotNull private String redirectString;

}
