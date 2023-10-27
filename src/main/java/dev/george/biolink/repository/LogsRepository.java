package dev.george.biolink.repository;

import dev.george.biolink.model.Log;
import jakarta.persistence.Table;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Table(name = "staff_logs", schema = "public")
@Repository
public interface LogsRepository extends JpaRepository<Log, Integer> {

    <S extends Log> List<S> findAllByTargetUser(int userId, Example<S> example);

    <S extends Log> List<S> findAllByStaffId(int staffId, Example<S> example);

}
