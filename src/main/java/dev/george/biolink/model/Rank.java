package dev.george.biolink.model;

import dev.george.biolink.entity.GrantedAuthorityImpl;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rank", schema = "public")
@Getter @Setter
public class Rank {

    @Id @NotNull private int id;

    private String name;
    private String style;

    private Integer priority;
    private Boolean purchasable;

    private Boolean canManageLower;
    private Boolean staff;

    private Boolean canBan;
    private Boolean canManageUsers;
    private Boolean canGiveRanks;
    private Boolean canGiveStaffRanks;

    public List<GrantedAuthority> getGrantedAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();

        authorities.add(new GrantedAuthorityImpl("user"));

        if (staff != null && staff) {
            authorities.add(new GrantedAuthorityImpl("staff"));
        }

        if (canManageLower != null && canManageLower) {
            authorities.add(new GrantedAuthorityImpl("admin"));
        }

        return authorities;
    }
}
