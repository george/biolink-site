package dev.george.biolink.database;

import dev.george.biolink.model.impl.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends CrudRepository<User, Integer> {

}
