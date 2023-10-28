package dev.george.biolink.service;

import dev.george.biolink.entity.UserGroupId;
import dev.george.biolink.exception.ProfileNotFoundException;
import dev.george.biolink.model.Rank;
import dev.george.biolink.repository.ProfileRepository;
import dev.george.biolink.entity.UserDetailsEntity;
import dev.george.biolink.model.Profile;
import dev.george.biolink.repository.RankRepository;
import dev.george.biolink.repository.UserGroupsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserProfileDetailsService extends InMemoryUserDetailsManager implements UserDetailsService {

    @Autowired
    private ProfileRepository profilesRepository;

    @Autowired
    private RankRepository rankRepository;

    @Autowired
    private UserGroupsRepository userGroupsRepository;

    public UserDetails loadUserById(int id) throws ProfileNotFoundException {
        Optional<Profile> optional = profilesRepository.findById(id);

        return optional.map((profile) -> new UserDetailsEntity(profile, getGroups(profile)))
                .orElseThrow(ProfileNotFoundException::new);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Profile> optional = profilesRepository.findOneByEmail(username);

        return optional.map((profile) -> new UserDetailsEntity(profile, getGroups(profile)))
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }

    private List<Rank> getGroups(Profile profile) {
        List<Integer> groupIds = userGroupsRepository.findManyByUserGroupIdUserId(profile.getId())
                .stream()
                .map(userGroup -> userGroup.getUserGroupId().getGroupId())
                .toList();

        return rankRepository.findAllByIdIn(groupIds);
    }
}
