package dev.george.biolink.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Table(name = "ban", schema = "public")
@Getter @Setter
public class Ban {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id @NotNull private int punishmentId;

    @NotNull private int userId;
    @NotNull private int banTypeId;
    @NotNull private Timestamp bannedAt;
    @NotNull private int issuedBy;
    @NotNull private boolean banActive;

}
