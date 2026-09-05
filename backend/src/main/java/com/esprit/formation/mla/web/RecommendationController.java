package com.esprit.formation.mla.web;

import com.esprit.formation.mla.RecommendationService;
import com.esprit.formation.mla.dto.RecommendedFormationResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mla")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/recommandations")
    public List<RecommendedFormationResponse> recommend() {
        return recommendationService.recommend();
    }
}
