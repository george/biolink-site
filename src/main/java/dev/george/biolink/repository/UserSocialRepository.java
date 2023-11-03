package dev.george.biolink.repository;

import dev.george.biolink.entity.UserSocialUserPlatformUsername;
import dev.george.biolink.model.Ban;
import dev.george.biolink.model.UserSocial;
import jakarta.persistence.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Table(name = "user_social", schema = "public")
@Repository
public interface UserSocialRepository extends JpaRepository<UserSocial, UserSocialUserPlatformUsername> {

}
