package dev.george.biolink.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Table(name = "ban_reason", schema = "public")
@Getter @Setter
public class BanReason {

    @Id @NotNull private int banId;

    @NotNull private String banReason;

}
