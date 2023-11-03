package dev.george.biolink.repository;

import dev.george.biolink.entity.ProfileComponentUserIdComponentIndex;
import dev.george.biolink.model.Ban;
import dev.george.biolink.model.ProfileComponent;
import jakarta.persistence.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Table(name = "profile_component", schema = "public")
@Repository
public interface ProfileComponentRepository extends JpaRepository<ProfileComponent, ProfileComponentUserIdComponentIndex> {

}
