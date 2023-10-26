package dev.george.biolink.bean;

import dev.george.biolink.service.UserProfileDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
public class UserDetailsBean {

    @Bean
    public UserDetailsService getUserDetailsService() {
        return new UserProfileDetailsService();
    }

}
