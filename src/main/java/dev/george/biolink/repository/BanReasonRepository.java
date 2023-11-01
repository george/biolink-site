package dev.george.biolink.repository;

import dev.george.biolink.model.BanReason;
import jakarta.persistence.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Table(name = "ban_reason", schema = "public")
@Repository
public interface BanReasonRepository extends JpaRepository<BanReason, Integer> {

    Optional<BanReason> findByBanReason(String banReason);

}
