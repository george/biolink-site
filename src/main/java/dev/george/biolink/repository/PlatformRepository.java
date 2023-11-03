package dev.george.biolink.repository;

import dev.george.biolink.model.Ban;
import dev.george.biolink.model.Platform;
import jakarta.persistence.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Table(name = "platform", schema = "public")
@Repository
public interface PlatformRepository extends JpaRepository<Platform, Integer> {

}
