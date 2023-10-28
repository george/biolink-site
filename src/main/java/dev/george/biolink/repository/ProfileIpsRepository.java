package dev.george.biolink.repository;

import dev.george.biolink.entity.ProfileIpId;
import dev.george.biolink.model.ProfileIp;
import jakarta.persistence.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Table(name = "profile_ip", schema = "public")
@Repository
public interface ProfileIpsRepository extends JpaRepository<ProfileIp, ProfileIpId> {

    List<ProfileIp> findAllByProfileIpIdProfileId(int profileIpId);

}
