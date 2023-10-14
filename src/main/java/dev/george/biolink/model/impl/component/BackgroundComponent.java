package dev.george.biolink.model.impl.component;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.george.biolink.model.JsonModel;
import dev.george.biolink.model.Model;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.List;

@Getter @Setter
public class BackgroundComponent<T> extends JsonModel<T> {

    private String imageUrl;
    private String backgroundColor;
    private List<String> backgroundGradient;

    @Override
    public void write(JsonWriter jsonWriter, T t) throws IOException {
        jsonWriter.beginObject();

        if (imageUrl != null) {
            jsonWriter.name("url");
            jsonWriter.value(imageUrl);
        } else if (backgroundGradient != null) {
            jsonWriter.name("gradient");

            jsonWriter.beginArray();

            for (String color : backgroundGradient) {
                jsonWriter.value(color);
            }

            jsonWriter.endArray();
        } else {
            jsonWriter.name("color");
            jsonWriter.value(backgroundColor);
        }

        jsonWriter.endObject();
    }

    @Override
    public T read(JsonReader jsonReader) throws IOException {
        return null;
    }
}
