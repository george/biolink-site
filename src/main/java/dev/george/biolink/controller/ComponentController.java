package dev.george.biolink.controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.george.biolink.entity.UserDetailsEntity;
import dev.george.biolink.model.Component;
import dev.george.biolink.model.Log;
import dev.george.biolink.model.Profile;
import dev.george.biolink.model.type.LogType;
import dev.george.biolink.repository.ComponentRepository;
import dev.george.biolink.repository.LogsRepository;
import dev.george.biolink.repository.ProfileComponentRepository;
import dev.george.biolink.schema.component.CreateComponentSchema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
public class ComponentController {

    private final ComponentRepository componentRepository;
    private final Gson gson;
    private final LogsRepository logsRepository;
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
            componentObject.addProperty("parent", component.getComponentParent());

            components.add(componentObject);
        });

        object.addProperty("success", true);
        object.add("components", components);

        return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(200));
    }

    @PostMapping(
            value = "/component/create",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> createComponent(
            @RequestBody CreateComponentSchema schema
    ) {
        JsonObject object = new JsonObject();
        UserDetailsEntity userDetails = (UserDetailsEntity) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        Profile profile = userDetails.getProfile();
        Component exampleComponent = new Component();

        exampleComponent.setComponentCreator(profile.getId());
        exampleComponent.setComponentName(schema.getComponentName());

        if (!componentRepository.findAll(Example.of(exampleComponent,
                ExampleMatcher.matchingAll().withIgnoreNullValues().withIgnoreCase())).isEmpty()) {
            object.addProperty("error", true);
            object.addProperty("error_code", "component_with_name_already_exists");

            return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(409));
        }

        if (exampleComponent.getComponentTag() == null) {
            Log log = new Log();

            log.setLogTypeId(LogType.USER_INVALID_INPUT.getType());
            log.setTargetUser(profile.getId());
            log.setDescription(String.format("Provided input %d", schema.getComponentTagId()));

            logsRepository.saveAndFlush(log);

            object.addProperty("error", true);
            object.addProperty("error_code", "invalid_tag");

            return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(409));
        }

        return new ResponseEntity<>(gson.toJson(object), HttpStatusCode.valueOf(201));
    }
}
