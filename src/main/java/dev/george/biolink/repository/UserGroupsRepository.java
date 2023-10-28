package dev.george.biolink.repository;

import dev.george.biolink.entity.UserGroupId;
import dev.george.biolink.model.UserGroup;
import jakarta.persistence.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Table(name = "user_group", schema = "public")
@Repository
public interface UserGroupsRepository extends JpaRepository<UserGroup, UserGroupId> {

    List<UserGroup> findManyByUserGroupIdUserId(int userId);

}
