package dev.george.biolink.schema.payment;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ChargeSchema {

    private int packageId;
    private String discountCode;

    private String stripeEmail;
    private String stripeToken;

}
