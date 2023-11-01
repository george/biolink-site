package dev.george.biolink.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "user_note", schema = "public")
@Getter @Setter
public class Note {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id @NotNull private int noteId;

    @NotNull private int userId;
    @NotNull private int staffId;
    @NotNull private String note;
    @NotNull private Date leftAt;

}
