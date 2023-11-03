package dev.george.biolink.repository;

import dev.george.biolink.model.Ban;
import dev.george.biolink.model.VerificationCode;
import jakarta.persistence.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Table(name = "verification_code", schema = "public")
@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Integer> {

}
