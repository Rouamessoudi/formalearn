package com.esprit.formation.session.dto;

import com.esprit.formation.common.domain.SessionStatus;

import java.time.LocalDate;

public class SessionResponse {

    private Long id;
    private Long formationId;
    private String formationTitle;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer capacity;
    private SessionStatus status;
    private long enrolledCount;
    private long remainingPlaces;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFormationId() {
        return formationId;
    }

    public void setFormationId(Long formationId) {
        this.formationId = formationId;
    }

    public String getFormationTitle() {
        return formationTitle;
    }

    public void setFormationTitle(String formationTitle) {
        this.formationTitle = formationTitle;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public long getEnrolledCount() {
        return enrolledCount;
    }

    public void setEnrolledCount(long enrolledCount) {
        this.enrolledCount = enrolledCount;
    }

    public long getRemainingPlaces() {
        return remainingPlaces;
    }

    public void setRemainingPlaces(long remainingPlaces) {
        this.remainingPlaces = remainingPlaces;
    }
}
