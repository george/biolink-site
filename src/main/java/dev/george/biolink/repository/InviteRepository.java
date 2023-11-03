package dev.george.biolink.repository;

import dev.george.biolink.entity.InviteUserIdCode;
import dev.george.biolink.model.Ban;
import dev.george.biolink.model.Invite;
import jakarta.persistence.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Table(name = "invite", schema = "public")
@Repository
public interface InviteRepository extends JpaRepository<Invite, InviteUserIdCode> {

}
