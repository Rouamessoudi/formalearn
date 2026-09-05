package com.esprit.formation.user.dto;

import com.esprit.formation.common.domain.EducationLevel;
import com.esprit.formation.common.domain.InterestArea;

import java.util.List;

public class ProfileResponse {

    private InterestArea interest;
    private Integer experienceYears;
    private EducationLevel educationLevel;
    private List<String> skills;

    public ProfileResponse(InterestArea interest, Integer experienceYears, EducationLevel educationLevel, List<String> skills) {
        this.interest = interest;
        this.experienceYears = experienceYears;
        this.educationLevel = educationLevel;
        this.skills = skills;
    }

    public InterestArea getInterest() {
        return interest;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public EducationLevel getEducationLevel() {
        return educationLevel;
    }

    public List<String> getSkills() {
        return skills;
    }
}
