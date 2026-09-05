package com.esprit.formation.mla.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MlFormationPayload {

    private long formationId;
    @JsonProperty("formation_category")
    private String formationCategory;
    private double price;
    @JsonProperty("duration_hours")
    private int durationHours;

    public long getFormationId() {
        return formationId;
    }

    public void setFormationId(long formationId) {
        this.formationId = formationId;
    }

    public String getFormationCategory() {
        return formationCategory;
    }

    public void setFormationCategory(String formationCategory) {
        this.formationCategory = formationCategory;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getDurationHours() {
        return durationHours;
    }

    public void setDurationHours(int durationHours) {
        this.durationHours = durationHours;
    }
}
