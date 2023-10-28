package dev.george.biolink.repository;

import dev.george.biolink.model.Context;
import jakarta.persistence.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Table(name = "context", schema = "public")
@Repository
public interface ContextRepository extends JpaRepository<Context, Integer> {

    Optional<Context> findContextByContextMeta(String meta);

}
