package dev.george.biolink.repository;

import dev.george.biolink.model.Discount;
import jakarta.persistence.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Table(name = "discount", schema = "public")
@Repository
public interface DiscountRepository extends JpaRepository<Discount, Integer> {

    Optional<Discount> findDiscountByPromotionCode(String promotionCode);

}
