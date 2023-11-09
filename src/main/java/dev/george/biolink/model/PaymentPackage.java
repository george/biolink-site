package dev.george.biolink.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Table(name = "payment_package", schema = "public")
@Getter @Setter
public class PaymentPackage {

    @Id private int id;

    private String name;
    private String description;
    
    private int rankId;
    private double price;

    private Timestamp availableFrom;
    private Timestamp availableTo;

}
