package dev.george.biolink.controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
public class ViewProfileController {

    private final Gson gson;

    @GetMapping(
            value = "/users",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> getProfile(
            @RequestParam String redirectName
    ) {
        JsonArray elements = new JsonArray();

        return new ResponseEntity<>(gson.toJson(elements), HttpStatusCode.valueOf(200));
    }
}
