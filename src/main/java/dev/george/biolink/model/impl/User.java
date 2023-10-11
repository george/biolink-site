package dev.george.biolink.model.impl;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.george.biolink.model.Model;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.List;

@Getter @Setter
public class User extends Model<User> {

    private int id;

    private List<Model<?>> pageComponents;

    private List<String> links;
    private List<String> domains;

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
