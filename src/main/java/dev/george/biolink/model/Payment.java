package dev.george.biolink.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "payment", schema = "public")
@Getter @Setter
public class Payment {

    @Id private int paymentId;

    private int userId;
    private int paymentType;

    private double paymentAmount;

    private String data;
    private int discountUsed;

}
