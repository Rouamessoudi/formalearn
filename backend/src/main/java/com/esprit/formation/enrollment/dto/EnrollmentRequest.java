package com.esprit.formation.enrollment.dto;

import jakarta.validation.constraints.NotNull;

public class EnrollmentRequest {

    @NotNull
    private Long sessionId;

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }
}
