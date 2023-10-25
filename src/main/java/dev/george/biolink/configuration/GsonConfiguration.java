package dev.george.biolink.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.george.biolink.model.Model;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AssignableTypeFilter;

@Configuration
public class GsonConfiguration {

    private GsonBuilder getGsonBuilder() {
        GsonBuilder builder = new GsonBuilder();

        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(Model.class));

        provider.findCandidateComponents(Model.class.getPackage().toString())
                        .forEach(typeAdapterModel -> {
                            builder.registerTypeAdapter(typeAdapterModel.getClass(), typeAdapterModel);
                        });

        return builder;
    }

    @Bean
    public Gson gson() {
        return getGsonBuilder().create();
    }
}