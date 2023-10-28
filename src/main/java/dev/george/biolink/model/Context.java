package dev.george.biolink.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Table(name = "context", schema = "public")
@Getter @Setter
public class Context {

    @Id private int contextId;

    @NotNull private int userId;
    private String contextMeta;

    @NotNull private Timestamp createdAt;

}
