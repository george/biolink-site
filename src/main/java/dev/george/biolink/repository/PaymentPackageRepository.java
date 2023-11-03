package dev.george.biolink.repository;

import dev.george.biolink.model.PaymentPackage;
import jakarta.persistence.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Table(name = "payment_package", schema = "public")
@Repository
public interface PaymentPackageRepository extends JpaRepository<PaymentPackage, Integer> {

}
