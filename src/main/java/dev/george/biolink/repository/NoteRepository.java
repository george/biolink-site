package dev.george.biolink.repository;

import dev.george.biolink.model.Note;
import jakarta.persistence.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Table(name = "user_note", schema = "public")
@Repository
public interface NoteRepository extends JpaRepository<Note, Integer> {

    <S extends Note> List<S> findAlByUserId(int userId);

}
