package com.esprit.formation.enrollment.dto;

import com.esprit.formation.catalogue.domain.Formation;
import com.esprit.formation.enrollment.domain.Enrollment;
import com.esprit.formation.session.domain.TrainingSession;

public final class EnrollmentMapper {

    private EnrollmentMapper() {
    }

    public static EnrollmentResponse toResponse(Enrollment enrollment) {
        TrainingSession session = enrollment.getSession();
        Formation formation = session.getFormation();
        EnrollmentResponse response = new EnrollmentResponse();
        response.setId(enrollment.getId());
        response.setStatus(enrollment.getStatus());
        response.setCreatedAt(enrollment.getCreatedAt());
        response.setSessionId(session.getId());
        response.setStartDate(session.getStartDate());
        response.setEndDate(session.getEndDate());
        response.setSessionStatus(session.getStatus());
        response.setFormationId(formation.getId());
        response.setFormationTitle(formation.getTitle());
        response.setCategoryName(formation.getCategory().getName());
        response.setFormationPrice(formation.getPrice());
        response.setLearnerId(enrollment.getUser().getId());
        response.setLearnerName(enrollment.getUser().getFullName());
        response.setLearnerEmail(enrollment.getUser().getEmail());
        response.setCapacity(session.getCapacity());
        return response;
    }
}
