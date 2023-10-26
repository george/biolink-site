package dev.george.biolink.repository;

import dev.george.biolink.model.Profile;
import jakarta.persistence.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Table(name = "profile", schema = "public")
@Repository
public interface ProfileRepository extends JpaRepository<Profile, Integer> {

    Optional<Profile> findOneByEmail(String email);

}
