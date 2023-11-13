package dev.george.biolink.controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.george.biolink.model.Component;
import dev.george.biolink.repository.ComponentRepository;
import dev.george.biolink.repository.ProfileComponentRepository;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.hibernate.boot.model.source.spi.Sortable;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
public class ComponentController {

    private final ComponentRepository componentRepository;
    private final Gson gson;
    private final ProfileComponentRepository profileComponentRepository;

    @GetMapping(
            value = "/component/get-public",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> getPublicComponents(
            @NotNull Integer pageNumber,
            @NotNull Integer maxPerPage,
            @Nullable Integer componentType
    ) {
        JsonObject object = new JsonObject();
        JsonArray components = new JsonArray();

        Component exampleComponent = new Component();
        exampleComponent.setComponentType(componentType);

        Page<Component> page = componentRepository.findAll(Example.of(exampleComponent,
                        ExampleMatcher.matchingAny().withIgnoreNullValues()),
                PageRequest.of(pageNumber, maxPerPage));

        page.forEach(component -> {
            JsonObject componentObject = new JsonObject();

            componentObject.addProperty("id", component.getComponentId());
            componentObject.addProperty("name", component.getComponentName());
            componentObject.addProperty("meta", component.getComponentMeta());
            componentObject.addProperty("text", component.getComponentText());
            componentObject.addProperty("styles", component.getComponentStyles());
            componentObject.addProperty("container", component.isComponentHasChildren());

            components.add(componentObject);
        });

        object.addProperty("success", true);
        object.add("components", components);

        return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(200));
    }
}
