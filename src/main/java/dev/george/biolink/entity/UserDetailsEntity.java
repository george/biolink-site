package dev.george.biolink.entity;

import dev.george.biolink.model.Profile;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.List;

@Getter
public class UserDetailsEntity implements UserDetails {

    private final Profile profile;

    private final String username;
    private final String password;

    private final List<GrantedAuthority> authorities = new ArrayList<>();

    public UserDetailsEntity(Profile profile) {
        this.profile = profile;

        this.username = profile.getUsername();
        this.password = profile.getPassword();

        this.authorities.add(new GrantedAuthorityImpl("user"));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
