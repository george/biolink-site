package dev.george.biolink.service;

import dev.george.biolink.exception.ProfileNotFoundException;
import dev.george.biolink.repository.ProfileRepository;
import dev.george.biolink.entity.UserDetailsEntity;
import dev.george.biolink.model.Profile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserProfileDetailsService extends InMemoryUserDetailsManager implements UserDetailsService {

    @Autowired
    private ProfileRepository profilesRepository;

    public UserDetails loadUserById(int id) throws ProfileNotFoundException {
        Optional<Profile> optional = profilesRepository.findById(id);

        return optional.map(UserDetailsEntity::new)
                .orElseThrow(ProfileNotFoundException::new);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Profile> optional = profilesRepository.findOneByEmail(username);

        return optional.map(UserDetailsEntity::new)
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }
}
