package com.esprit.formation.mla;

import com.esprit.formation.catalogue.domain.Formation;
import com.esprit.formation.catalogue.dto.CatalogueMapper;
import com.esprit.formation.catalogue.repository.FormationRepository;
import com.esprit.formation.common.domain.FormationStatus;
import com.esprit.formation.common.exception.ApiException;
import com.esprit.formation.config.MlProperties;
import com.esprit.formation.mla.dto.MlPredictRequest;
import com.esprit.formation.mla.dto.MlScoredFormation;
import com.esprit.formation.mla.dto.RecommendedFormationResponse;
import com.esprit.formation.user.domain.Profile;
import com.esprit.formation.user.service.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final ProfileService profileService;
    private final FormationRepository formationRepository;
    private final MlScoringClient mlScoringClient;
    private final MlProperties mlProperties;

    public RecommendationService(
            ProfileService profileService,
            FormationRepository formationRepository,
            MlScoringClient mlScoringClient,
            MlProperties mlProperties
    ) {
        this.profileService = profileService;
        this.formationRepository = formationRepository;
        this.mlScoringClient = mlScoringClient;
        this.mlProperties = mlProperties;
    }

    @Transactional(readOnly = true)
    public List<RecommendedFormationResponse> recommend() {
        Profile profile = profileService.requireCurrentProfile();
        List<Formation> published = formationRepository.findAllByStatusWithCategory(FormationStatus.PUBLISHED);
        if (published.isEmpty()) {
            return List.of();
        }
        if (published.stream().anyMatch(item -> item.getStatus() != FormationStatus.PUBLISHED)) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Une formation non publiée a été envoyée au moteur ML");
        }
        MlPredictRequest request = MlFeatureMapper.toRequest(profile, published);
        List<MlScoredFormation> scored = mlScoringClient.score(request);
        Map<Long, Formation> byId = published.stream().collect(Collectors.toMap(Formation::getId, Function.identity()));
        return scored.stream()
                .sorted(Comparator.comparingDouble(MlScoredFormation::getScore).reversed())
                .filter(item -> byId.containsKey(item.getFormationId()))
                .limit(mlProperties.getTopN())
                .map(item -> toResponse(byId.get(item.getFormationId()), item.getScore()))
                .toList();
    }

    private RecommendedFormationResponse toResponse(Formation formation, double score) {
        RecommendedFormationResponse response = new RecommendedFormationResponse();
        response.setId(formation.getId());
        response.setTitle(formation.getTitle());
        response.setDescription(formation.getDescription());
        response.setCategory(CatalogueMapper.toCategory(formation.getCategory()));
        response.setPrice(formation.getPrice());
        response.setDurationHours(formation.getDurationHours());
        response.setStatus(formation.getStatus());
        response.setScore(score);
        return response;
    }
}
