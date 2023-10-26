package dev.george.biolink.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.session.CookieWebSessionIdResolver;
import org.springframework.web.server.session.WebSessionIdResolver;

import java.time.Duration;
import java.time.temporal.TemporalUnit;

@Configuration
public class CookieConfig {

    @Value("${biolink.cookie.domain}")
    private String cookieDomain;

    @Bean
    public WebSessionIdResolver webSessionIdResolver() {
        CookieWebSessionIdResolver resolver = new CookieWebSessionIdResolver();

        resolver.setCookieName("session");
        resolver.setCookieMaxAge(Duration.ofDays(30L));

        resolver.addCookieInitializer((builder) -> {
            builder.path("/");
            builder.domain(cookieDomain);
            builder.sameSite("Strict");
            builder.secure(true);
        });

        return resolver;
    }
}
