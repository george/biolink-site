package dev.george.biolink.repository;

import dev.george.biolink.model.Payment;
import jakarta.persistence.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Table(name = "payment", schema = "public")
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    List<Payment> findPaymentsByUserId(int userId);

}
