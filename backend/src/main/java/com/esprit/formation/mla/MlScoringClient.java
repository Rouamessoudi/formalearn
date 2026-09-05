package com.esprit.formation.mla;

import com.esprit.formation.mla.dto.MlPredictRequest;
import com.esprit.formation.mla.dto.MlScoredFormation;

import java.util.List;

public interface MlScoringClient {
    List<MlScoredFormation> score(MlPredictRequest request);
}
