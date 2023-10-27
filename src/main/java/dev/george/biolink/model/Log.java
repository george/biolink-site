package dev.george.biolink.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "staff_logs", schema = "public")
@Getter @Setter
public class Log {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id @NotNull private int logId;

    @NotNull private int logTypeId;
    @NotNull private int staffId;

    private int targetUser;
    private String description;

}
