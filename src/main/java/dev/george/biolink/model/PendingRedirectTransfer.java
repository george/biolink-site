package dev.george.biolink.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Table(name = "pending_redirect_transfers", schema = "public")
@Getter @Setter
public class PendingRedirectTransfer {

    @Id @NotNull private String pendingRedirectString;

    @NotNull private int transferringTo;

}
