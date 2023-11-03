package dev.george.biolink.model;

import dev.george.biolink.entity.DomainUserIdName;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "domain", schema = "public")
public class Domain {

    @EmbeddedId private DomainUserIdName key;

}
