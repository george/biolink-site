package dev.george.biolink.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "rank", schema = "public")
@Getter @Setter
public class Rank {

    @Id @NotNull private long id;

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

}
