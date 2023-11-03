package dev.george.biolink.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Table(name = "discount", schema = "public")
@Getter @Setter
public class Discount {

    @Id private int id;

    private String name;

    private double discountAmount;

    private String promotionCode;

    private Timestamp availableFrom;
    private Timestamp availableTo;
}
