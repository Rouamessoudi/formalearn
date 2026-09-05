package com.esprit.formation.enrollment.dto;

import com.esprit.formation.common.domain.EnrollmentStatus;
import jakarta.validation.constraints.NotNull;

public class EnrollmentStatusRequest {

    @NotNull
    private EnrollmentStatus status;

    public EnrollmentStatus getStatus() {
        return status;
    }

    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }
}
