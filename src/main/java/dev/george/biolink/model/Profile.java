package dev.george.biolink.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Table(name = "profile", schema = "public")
@Getter @Setter
public class Profile {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id private Integer id;

    @NotNull private String username;
    @NotNull private String password;
    @NotNull private String email;

    @NotNull private Integer lastIp;
    @NotNull private String ipSalt;

    @NotNull private Timestamp createdAt;
    @NotNull private Timestamp lastLogin;

    private Boolean mfaEnabled;
    private String mfaSecret;

    private Integer invitedBy;

    public String getLastIpString() {
        return String.format("%d.%d.%d.%d",
                (lastIp >> 24 & 0xff),
                (lastIp >> 16 & 0xff),
                (lastIp >> 8 & 0xff),
                (lastIp & 0xff)
                );
    }

    public void setLastIp(String ip) {
        String[] parts = ip.split("\\.");

        int ipNumbers = 0;
        for (int i = 0; i < 4; i++) {
            ipNumbers += Integer.parseInt(parts[i]) << (24 - (8 * i));
        }

        this.lastIp = ipNumbers;
    }
}
