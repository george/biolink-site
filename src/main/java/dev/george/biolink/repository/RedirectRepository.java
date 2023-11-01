package dev.george.biolink.repository;

import dev.george.biolink.model.Redirect;
import jakarta.persistence.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Table(name = "profile_redirect", schema = "public")
@Repository
public interface RedirectRepository extends JpaRepository<Redirect, Integer> {

    Optional<Redirect> findByRedirectString(String redirectString);

    List<Redirect> findByUserId(int userId);

}
