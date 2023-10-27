package dev.george.biolink.controller;

import dev.george.biolink.repository.BansRepository;
import dev.george.biolink.repository.ProfileRepository;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class AdminController {

    private final BansRepository bansRepository;
    private final ProfileRepository profileRepository;

}
