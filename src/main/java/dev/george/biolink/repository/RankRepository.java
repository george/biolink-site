package dev.george.biolink.repository;

import dev.george.biolink.model.Rank;
import jakarta.persistence.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Table(name = "rank", schema = "public")
@Repository
public interface RankRepository extends JpaRepository<Rank, Integer> {

    List<Rank> findAll();

}
