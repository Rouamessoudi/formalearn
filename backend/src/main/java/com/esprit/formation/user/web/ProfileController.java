package com.esprit.formation.user.web;

import com.esprit.formation.user.dto.ProfileRequest;
import com.esprit.formation.user.dto.ProfileResponse;
import com.esprit.formation.user.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profil")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ProfileResponse get() {
        return profileService.me();
    }

    @PutMapping
    public ProfileResponse update(@Valid @RequestBody ProfileRequest request) {
        return profileService.update(request);
    }
}
