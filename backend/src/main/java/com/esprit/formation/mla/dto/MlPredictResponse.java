package com.esprit.formation.mla.dto;

import java.util.ArrayList;
import java.util.List;

public class MlPredictResponse {

    private List<MlScoredFormation> recommendations = new ArrayList<>();

    public List<MlScoredFormation> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<MlScoredFormation> recommendations) {
        this.recommendations = recommendations;
    }
}
