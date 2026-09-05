package com.esprit.formation.mla;

import com.esprit.formation.catalogue.domain.Formation;
import com.esprit.formation.mla.dto.MlFormationPayload;
import com.esprit.formation.mla.dto.MlPredictRequest;
import com.esprit.formation.mla.dto.MlProfilePayload;
import com.esprit.formation.user.domain.Profile;
import com.esprit.formation.user.service.ProfileService;

import java.util.List;
import java.util.Locale;

public final class MlFeatureMapper {

    private MlFeatureMapper() {
    }

    public static MlPredictRequest toRequest(Profile profile, List<Formation> formations) {
        MlPredictRequest request = new MlPredictRequest();
        request.setProfile(toProfile(profile));
        request.setFormations(formations.stream().map(MlFeatureMapper::toFormation).toList());
        return request;
    }

    public static String categoryCode(String categoryName) {
        if (categoryName == null) {
            return "OTHER";
        }
        String normalized = categoryName.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
            String ascii = java.text.Normalizer.normalize(normalized, java.text.Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "")
                    .replace("&", "ET")
                    .replaceAll("[^A-Z0-9_]", "_")
                    .replaceAll("_+", "_");
            if (ascii.contains("WEB") || ascii.contains("JAVA") || ascii.contains("DEVOPS")
                    || ascii.contains("CLOUD") || ascii.contains("CYBER")) {
                return "INFORMATIQUE";
            }
            if (ascii.contains("INTELLIGENCE") || ascii.contains("DONNE") || ascii.contains("DATA")) {
                return "DATA_SCIENCE";
            }
            return switch (normalized) {
                case "INFORMATIQUE" -> "INFORMATIQUE";
                case "DATA_SCIENCE", "DATASCIENCE" -> "DATA_SCIENCE";
                case "BUSINESS" -> "BUSINESS";
                case "LANGUES", "LANGUAGES" -> "LANGUES";
                default -> ascii;
            };
    }

    private static MlProfilePayload toProfile(Profile profile) {
        MlProfilePayload payload = new MlProfilePayload();
        payload.setInterest(profile.getInterest().name());
        payload.setExperienceYears(profile.getExperienceYears());
        payload.setEducationLevel(profile.getEducationLevel().name());
        payload.setHasJava(ProfileService.hasSkill(profile, "JAVA") ? 1 : 0);
        payload.setHasSpring(ProfileService.hasSkill(profile, "SPRING") ? 1 : 0);
        payload.setHasSql(ProfileService.hasSkill(profile, "SQL") ? 1 : 0);
        payload.setHasPython(ProfileService.hasSkill(profile, "PYTHON") ? 1 : 0);
        payload.setHasManagement(ProfileService.hasSkill(profile, "MANAGEMENT") ? 1 : 0);
        return payload;
    }

    private static MlFormationPayload toFormation(Formation formation) {
        MlFormationPayload payload = new MlFormationPayload();
        payload.setFormationId(formation.getId());
        payload.setFormationCategory(categoryCode(formation.getCategory().getName()));
        payload.setPrice(formation.getPrice().doubleValue());
        payload.setDurationHours(formation.getDurationHours());
        return payload;
    }
}
