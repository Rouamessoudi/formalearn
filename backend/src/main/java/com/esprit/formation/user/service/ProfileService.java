package com.esprit.formation.user.service;

import com.esprit.formation.common.exception.ApiException;
import com.esprit.formation.security.SecurityUtils;
import com.esprit.formation.user.domain.Profile;
import com.esprit.formation.user.domain.User;
import com.esprit.formation.user.dto.ProfileRequest;
import com.esprit.formation.user.dto.ProfileResponse;
import com.esprit.formation.user.repository.ProfileRepository;
import com.esprit.formation.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@Transactional
public class ProfileService {

    private static final Set<String> ALLOWED_SKILLS = Set.of("JAVA", "SPRING", "SQL", "PYTHON", "MANAGEMENT");

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    public ProfileService(ProfileRepository profileRepository, UserRepository userRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public ProfileResponse me() {
        return toResponse(requireCurrentProfile());
    }

    public ProfileResponse update(ProfileRequest request) {
        User user = currentUser();
        Profile profile = profileRepository.findByUserId(user.getId()).orElseGet(() -> {
            Profile created = new Profile();
            created.setUser(user);
            user.setProfile(created);
            return created;
        });
        profile.setInterest(request.getInterest());
        profile.setExperienceYears(request.getExperienceYears());
        profile.setEducationLevel(request.getEducationLevel());
        profile.setSkillTags(normalizeSkills(request.getSkills()));
        userRepository.save(user);
        return toResponse(profile);
    }

    @Transactional(readOnly = true)
    public Profile requireCurrentProfile() {
        return profileRepository.findByUserId(currentUser().getId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST,
                        "Complétez votre profil avant de demander des recommandations"));
    }

    private User currentUser() {
        return userRepository.findById(SecurityUtils.requireUser().getId())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Utilisateur introuvable"));
    }

    public static List<String> parseSkills(String skillTags) {
        if (skillTags == null || skillTags.isBlank()) {
            return List.of();
        }
        return Arrays.stream(skillTags.split(","))
                .map(value -> value.trim().toUpperCase(Locale.ROOT))
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();
    }

    public static boolean hasSkill(Profile profile, String skill) {
        return parseSkills(profile.getSkillTags()).contains(skill);
    }

    private String normalizeSkills(List<String> skills) {
        if (skills == null) {
            return "";
        }
        List<String> cleaned = skills.stream()
                .map(value -> value == null ? "" : value.trim().toUpperCase(Locale.ROOT))
                .filter(ALLOWED_SKILLS::contains)
                .distinct()
                .toList();
        return String.join(",", cleaned);
    }

    private ProfileResponse toResponse(Profile profile) {
        return new ProfileResponse(
                profile.getInterest(),
                profile.getExperienceYears(),
                profile.getEducationLevel(),
                parseSkills(profile.getSkillTags())
        );
    }
}
