package dev.george.biolink.repository;

import dev.george.biolink.entity.DomainUserIdName;
import dev.george.biolink.model.Ban;
import dev.george.biolink.model.Domain;
import jakarta.persistence.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Table(name = "domain", schema = "public")
@Repository
public interface DomainRepository extends JpaRepository<Domain, DomainUserIdName> {

}
