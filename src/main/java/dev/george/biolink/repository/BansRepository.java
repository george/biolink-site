package dev.george.biolink.repository;

import dev.george.biolink.model.Ban;
import jakarta.persistence.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Table(name = "punishments", schema = "public")
@Repository
public interface BansRepository extends JpaRepository<Ban, Integer> {

    List<Ban> findBansByUserId(int userId);

}
