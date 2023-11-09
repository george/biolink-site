package dev.george.biolink.repository;

import dev.george.biolink.model.PaymentPackage;
import jakarta.persistence.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Table(name = "payment_package", schema = "public")
@Repository
public interface PaymentPackageRepository extends JpaRepository<PaymentPackage, Integer> {

    Optional<PaymentPackage> findById(int id);

    List<PaymentPackage> getAllByIdIn(List<Integer> ids);

}
