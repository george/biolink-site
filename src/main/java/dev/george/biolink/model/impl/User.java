package dev.george.biolink.model.impl;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.george.biolink.model.Model;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

@Entity
@Getter @Setter
public class User extends Model<User> {

    @Id
    private long id;

    private String username;
    private String email;
    private String domain;

    private int lastIp;

    private long createdAt;
    private long lastLogin;

    @Override
    public void write(JsonWriter jsonWriter, User user) throws IOException {
        jsonWriter.beginObject();

        
    }

    @Override
    public User read(JsonReader jsonReader) throws IOException {
        return null;
    }
}
