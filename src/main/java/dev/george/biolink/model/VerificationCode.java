package dev.george.biolink.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "verificaton_code", schema = "public")
@Getter @Setter
public class VerificationCode {

    @Id private int userId;
    private int verificationCode;
    private String contextId;

}
