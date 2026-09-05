package com.esprit.formation.session.dto;

import com.esprit.formation.session.domain.TrainingSession;

public final class SessionMapper {

    private SessionMapper() {
    }

    public static SessionResponse toResponse(TrainingSession session, long enrolledCount) {
        SessionResponse response = new SessionResponse();
        response.setId(session.getId());
        response.setFormationId(session.getFormation().getId());
        response.setFormationTitle(session.getFormation().getTitle());
        response.setStartDate(session.getStartDate());
        response.setEndDate(session.getEndDate());
        response.setCapacity(session.getCapacity());
        response.setStatus(session.getStatus());
        response.setEnrolledCount(enrolledCount);
        response.setRemainingPlaces(Math.max(0, session.getCapacity() - enrolledCount));
        return response;
    }
}
