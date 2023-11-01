package dev.george.biolink.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "component", schema = "public")
@Getter
@Setter
public class Component {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id @NotNull
    private int componentId;

    private int componentType;
    private String componentMeta;
    private String componentText;
    private String componentStyles;

}
