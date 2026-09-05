package com.esprit.formation.mla.dto;

import java.util.ArrayList;
import java.util.List;

public class MlPredictRequest {

    private MlProfilePayload profile;
    private List<MlFormationPayload> formations = new ArrayList<>();

    public MlProfilePayload getProfile() {
        return profile;
    }

    public void setProfile(MlProfilePayload profile) {
        this.profile = profile;
    }

    public List<MlFormationPayload> getFormations() {
        return formations;
    }

    public void setFormations(List<MlFormationPayload> formations) {
        this.formations = formations;
    }
}
