package dev.george.biolink.controller;

import dev.george.biolink.entity.ProfileComponentUserIdComponentIndex;
import dev.george.biolink.model.Component;
import dev.george.biolink.model.ProfileComponent;
import dev.george.biolink.model.Redirect;
import dev.george.biolink.repository.ComponentRepository;
import dev.george.biolink.repository.ProfileComponentRepository;
import dev.george.biolink.repository.RedirectRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

@AllArgsConstructor
@RestController
public class ViewProfileController {

    private final ComponentRepository componentRepository;
    private final ProfileComponentRepository profileComponentRepository;
    private final RedirectRepository redirectRepository;

    @GetMapping(
            value = "/users",
            produces = MediaType.TEXT_HTML_VALUE
    )
    public ResponseEntity<String> getProfile(
            @RequestParam String redirectName
    ) {
        StringBuilder htmlResponse = new StringBuilder();

        Optional<Redirect> redirect = redirectRepository.findByRedirectString(redirectName.toLowerCase());

        if (redirect.isEmpty()) {
            return new ResponseEntity<>(htmlResponse.toString(), HttpStatusCode.valueOf(404));
        }

        ProfileComponentUserIdComponentIndex index = new ProfileComponentUserIdComponentIndex();
        index.setUserId(redirect.get().getUserId());

        ProfileComponent profileComponent = new ProfileComponent();
        profileComponent.setKey(index);

        List<ProfileComponent> profileComponents = profileComponentRepository.findAll(
                Example.of(profileComponent, ExampleMatcher.matchingAny().withIgnoreNullValues()));

        List<Component> components = componentRepository.findComponentsByComponentIdIn(profileComponents.stream()
                .sorted(Comparator.comparingInt(ProfileComponent::getComponentIndex))
                .map(ProfileComponent::getComponentId)
                .toList());

        Stack<Component> openTags = new Stack<>();

        htmlResponse.append("<main>");

        components.forEach(component -> {
            StringBuilder componentDataBuilder = new StringBuilder();

            componentDataBuilder.append("<").append(component.getComponentTag());

            if (component.getComponentMeta() != null) {
                componentDataBuilder.append(" ").append(component.getComponentMeta());
            }

            if (component.getComponentStyles() != null) {
                componentDataBuilder.append(" class=\"").append(component.getComponentStyles()).append("\"");
            }

            componentDataBuilder.append(component.isComponentHasChildren() ? "/" : "").append(">");

            if (component.isComponentHasChildren()) {
                openTags.push(component);
            }

            if (component.isComponentEndChildren()) {
                Component endingComponent = openTags.pop();

                componentDataBuilder.append("</").append(endingComponent.getComponentTag()).append(">");
            }

            htmlResponse.append(componentDataBuilder);
        });

        htmlResponse.append("</main>");

        return new ResponseEntity<>(htmlResponse.toString(), HttpStatusCode.valueOf(200));
    }
}
