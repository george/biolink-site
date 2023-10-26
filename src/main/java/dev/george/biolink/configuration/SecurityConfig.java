package dev.george.biolink.configuration;

import dev.george.biolink.component.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @Value("${biolink.cors.domain}")
    private String domain;

    @Autowired
    private JwtAuthenticationFilter filter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .cors((cors) -> {
                    cors.configurationSource(request -> {
                        CorsConfiguration corsConfig = new CorsConfiguration();

                        corsConfig.applyPermitDefaultValues();
                        corsConfig.setAllowCredentials(true);

                        corsConfig.addAllowedMethod("*");

                        corsConfig.setAllowedOrigins(Arrays.asList(
                                "http://localhost:8080",
                                "http://127.0.0.1:8080",
                                "https://*." + domain
                        ));

                        return corsConfig;
                    });
                })
                .authorizeHttpRequests((authorize) -> {
                    authorize.requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll();
                    authorize.requestMatchers("/auth/**").permitAll();

                    authorize.requestMatchers("/staff/**").hasAuthority("staff");
                    authorize.requestMatchers("/admin/**").hasAuthority("admin");

                    authorize.requestMatchers("/**").hasAuthority("user");
                }).addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class).build();
    }

    private String getDomain(String origin) {
        if (origin == null || origin.split("://").length == 0) {
            return origin;
        }

        String path = origin.split("://")[1];

        if (path.contains("/")) {
            path = path.split("/")[0];
        }

        if (path.contains(":")) {
            path = path.split(":")[0];
        }

        if (path.contains("localhost")) {
            path = path.replace("localhost", "127.0.0.1");
        }

        return path;
    }
}