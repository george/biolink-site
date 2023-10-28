package dev.george.biolink.entity;

import dev.george.biolink.model.Profile;
import dev.george.biolink.model.Rank;
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

    private final List<Rank> groups;

    public UserDetailsEntity(Profile profile, List<Rank> groups) {
        this.profile = profile;

        this.username = profile.getUsername();
        this.password = profile.getPassword();

        this.groups = groups;

        groups.stream()
                .flatMap(rank -> rank.getGrantedAuthorities().stream())
                .distinct()
                .forEach(this.authorities::add);
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
