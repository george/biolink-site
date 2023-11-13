package dev.george.biolink.repository;

import dev.george.biolink.model.Component;
import jakarta.persistence.Table;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Table(name = "component", schema = "public")
@Repository
public interface ComponentRepository extends JpaRepository<Component, Integer> {

    List<Component> findComponentsByComponentIdIn(List<Integer> ids);

    @Query(value = "SELECT DISTINCT c, COUNT(p.componentId) FROM Component c LEFT JOIN ProfileComponent p ON c.componentId = p.componentId GROUP BY c.componentId ORDER BY COUNT(p.componentId)")
    <S extends Component> Page<S> findAll(Example<S> example, Pageable pageable);

}
