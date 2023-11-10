package dev.george.biolink.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter @Setter
public class ChargeEntity {

    private final Map<String, Object> metadata = new HashMap<>();

    private String name;
    private String description;

    private double amount;

    private String paymentLink;

}