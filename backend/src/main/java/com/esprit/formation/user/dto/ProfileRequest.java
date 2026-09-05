package com.esprit.formation.user.dto;

import com.esprit.formation.common.domain.EducationLevel;
import com.esprit.formation.common.domain.InterestArea;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ProfileRequest {

    @NotNull
    private InterestArea interest;

    @NotNull
    @Min(0)
    @Max(40)
    private Integer experienceYears;

    @NotNull
    private EducationLevel educationLevel;

    private List<String> skills = new ArrayList<>();

    public InterestArea getInterest() {
        return interest;
    }

    public void setInterest(InterestArea interest) {
        this.interest = interest;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public EducationLevel getEducationLevel() {
        return educationLevel;
    }

    public void setEducationLevel(EducationLevel educationLevel) {
        this.educationLevel = educationLevel;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }
}
