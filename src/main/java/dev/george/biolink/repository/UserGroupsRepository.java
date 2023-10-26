package dev.george.biolink.repository;

import dev.george.biolink.entity.UserGroupId;
import dev.george.biolink.model.UserGroup;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface UserGroupsRepository extends Repository<UserGroup, UserGroupId> {

    List<UserGroup> findManyByUserGroupIdUserId(int userId);

}
