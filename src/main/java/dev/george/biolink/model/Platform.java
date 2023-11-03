package dev.george.biolink.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "platform", schema = "public")
@Getter @Setter
public class Platform {

    @Id private int platformId;
    private String platformDisplayName;

}
