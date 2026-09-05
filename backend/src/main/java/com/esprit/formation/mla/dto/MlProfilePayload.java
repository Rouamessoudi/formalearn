package com.esprit.formation.mla.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MlProfilePayload {

    private String interest;
    @JsonProperty("experience_years")
    private int experienceYears;
    @JsonProperty("education_level")
    private String educationLevel;
    @JsonProperty("has_java")
    private int hasJava;
    @JsonProperty("has_spring")
    private int hasSpring;
    @JsonProperty("has_sql")
    private int hasSql;
    @JsonProperty("has_python")
    private int hasPython;
    @JsonProperty("has_management")
    private int hasManagement;

    public String getInterest() {
        return interest;
    }

    public void setInterest(String interest) {
        this.interest = interest;
    }

    public int getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(int experienceYears) {
        this.experienceYears = experienceYears;
    }

    public String getEducationLevel() {
        return educationLevel;
    }

    public void setEducationLevel(String educationLevel) {
        this.educationLevel = educationLevel;
    }

    public int getHasJava() {
        return hasJava;
    }

    public void setHasJava(int hasJava) {
        this.hasJava = hasJava;
    }

    public int getHasSpring() {
        return hasSpring;
    }

    public void setHasSpring(int hasSpring) {
        this.hasSpring = hasSpring;
    }

    public int getHasSql() {
        return hasSql;
    }

    public void setHasSql(int hasSql) {
        this.hasSql = hasSql;
    }

    public int getHasPython() {
        return hasPython;
    }

    public void setHasPython(int hasPython) {
        this.hasPython = hasPython;
    }

    public int getHasManagement() {
        return hasManagement;
    }

    public void setHasManagement(int hasManagement) {
        this.hasManagement = hasManagement;
    }
}
