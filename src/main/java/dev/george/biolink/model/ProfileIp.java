package dev.george.biolink.model;

import dev.george.biolink.entity.ProfileIpId;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "profile_ip", schema = "public")
@Getter @Setter
public class ProfileIp {

    @EmbeddedId private ProfileIpId profileIpId;

}
