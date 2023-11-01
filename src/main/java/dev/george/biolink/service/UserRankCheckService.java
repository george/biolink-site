package dev.george.biolink.service;

import dev.george.biolink.model.Profile;
import dev.george.biolink.model.Rank;
import dev.george.biolink.repository.RankRepository;
import dev.george.biolink.repository.UserGroupsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Predicate;

@AllArgsConstructor
@Service
public class UserRankCheckService {

    private final RankRepository rankRepository;
    private final UserGroupsRepository groupsRepository;

    public boolean isUserGroupHigher(Profile first, Profile second) {
        return getGroups(first.getId()).stream().mapToInt(Rank::getPriority).max().orElse(0) >
                getGroups(second.getId()).stream().mapToInt(Rank::getPriority).max().orElse(0);
    }

    public boolean anyGroupsMatch(Profile profile, Predicate<Rank> predicate) {
        return getGroups(profile.getId()).stream().anyMatch(predicate);
    }

    public int getMaxRedirects(Profile profile) {
        return getMaxRedirects(getGroups(profile.getId()));
    }

    public int getMaxRedirects(List<Rank> groups) {
        return groups.stream().mapToInt(Rank::getMaxRedirects).max().orElse(3);
    }

    public List<Rank> getGroups(int userId) {
        return rankRepository.findAllByIdIn(
                groupsRepository.findManyByUserGroupIdUserId(userId)
                        .stream()
                        .map(group -> group.getUserGroupId().getGroupId())
                        .toList()
        );
    }
}
