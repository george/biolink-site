package dev.george.biolink.bean;

import dev.george.biolink.service.UserProfileDetailsService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class AuthenticationProviderBean {

    private final BcryptBean passwordEncoder;
    private final UserProfileDetailsService userDetailsService;

    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();

        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder.encoder());

        return authenticationProvider;
    }
}
