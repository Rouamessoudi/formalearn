package com.esprit.formation.enrollment.dto;

import com.esprit.formation.common.domain.EnrollmentStatus;
import com.esprit.formation.common.domain.SessionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class EnrollmentResponse {

    private Long id;
    private EnrollmentStatus status;
    private Instant createdAt;
    private Long sessionId;
    private LocalDate startDate;
    private LocalDate endDate;
    private SessionStatus sessionStatus;
    private Long formationId;
    private String formationTitle;
    private String categoryName;
    private BigDecimal formationPrice;
    private Long learnerId;
    private String learnerName;
    private String learnerEmail;
    private Integer capacity;
    private long enrolledCount;
    private long remainingPlaces;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
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

    public SessionStatus getSessionStatus() {
        return sessionStatus;
    }

    public void setSessionStatus(SessionStatus sessionStatus) {
        this.sessionStatus = sessionStatus;
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

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public BigDecimal getFormationPrice() {
        return formationPrice;
    }

    public void setFormationPrice(BigDecimal formationPrice) {
        this.formationPrice = formationPrice;
    }

    public Long getLearnerId() {
        return learnerId;
    }

    public void setLearnerId(Long learnerId) {
        this.learnerId = learnerId;
    }

    public String getLearnerName() {
        return learnerName;
    }

    public void setLearnerName(String learnerName) {
        this.learnerName = learnerName;
    }

    public String getLearnerEmail() {
        return learnerEmail;
    }

    public void setLearnerEmail(String learnerEmail) {
        this.learnerEmail = learnerEmail;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
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
