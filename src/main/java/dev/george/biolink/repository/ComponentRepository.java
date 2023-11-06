package dev.george.biolink.repository;

import dev.george.biolink.model.Component;
import dev.george.biolink.model.Context;
import jakarta.persistence.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Table(name = "component", schema = "public")
@Repository
public interface ComponentRepository extends JpaRepository<Component, Integer> {

    List<Component> findComponentsByComponentIdIn(List<Integer> ids);

}
