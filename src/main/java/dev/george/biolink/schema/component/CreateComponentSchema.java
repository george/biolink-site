package dev.george.biolink.schema.component;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.mapping.Component;

import java.util.Arrays;

@Getter @Setter
public class CreateComponentSchema {

    private int componentType;

    private int componentTagId;

    private String componentMeta;
    private String componentTag;
    private String componentStyles;
    private String componentName;

    private boolean isPublic;
    private boolean hasChildren;
    private boolean endChildren;

    public String getTagName() {
        ComponentTagType type = ComponentTagType.findById(componentTagId);

        return type == null ? null : type.getName();
    }

    @Getter @AllArgsConstructor
    public enum ComponentTagType {

        DIV("div", 0),
        LI("li", 1),
        UL("ul", 2),
        P("p", 3),
        H1("h1", 4),
        H2("h2", 5),
        H3("h3", 6),
        H4("h4", 7),
        H5("h5", 8),
        H6("h6", 9),
        SPAN("span", 10),
        IMG("img", 11),
        VIDEO("video", 12),
        BR("br", 13),
        A("a", 14),
        TABLE("table", 15),
        TR("tr", 16),
        TD("td", 17),
        AUDIO("audio", 18);

        private final String name;
        private final int id;

        public static ComponentTagType findById(int id) {
            return Arrays.stream(values()).filter(tag -> tag.getId() == id)
                    .findFirst()
                    .orElse(null);
        }
    }

    public boolean isValid(String input, int maxLength) {
        return input.length() <= maxLength && !input.contains("<");
    }
}
