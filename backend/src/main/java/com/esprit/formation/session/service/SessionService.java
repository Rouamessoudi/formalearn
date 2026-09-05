package com.esprit.formation.session.service;

import com.esprit.formation.catalogue.domain.Formation;
import com.esprit.formation.catalogue.repository.FormationRepository;
import com.esprit.formation.catalogue.service.FormationService;
import com.esprit.formation.common.domain.FormationStatus;
import com.esprit.formation.common.exception.ApiException;
import com.esprit.formation.enrollment.repository.EnrollmentRepository;
import com.esprit.formation.security.SecurityUtils;
import com.esprit.formation.session.domain.TrainingSession;
import com.esprit.formation.session.dto.SessionMapper;
import com.esprit.formation.session.dto.SessionRequest;
import com.esprit.formation.session.dto.SessionResponse;
import com.esprit.formation.session.repository.TrainingSessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SessionService {

    private final TrainingSessionRepository sessionRepository;
    private final FormationRepository formationRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final FormationService formationService;

    public SessionService(
            TrainingSessionRepository sessionRepository,
            FormationRepository formationRepository,
            EnrollmentRepository enrollmentRepository,
            FormationService formationService
    ) {
        this.sessionRepository = sessionRepository;
        this.formationRepository = formationRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.formationService = formationService;
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> findAll() {
        return sessionRepository.findAllWithFormation().stream()
                .filter(this::visibleToCaller)
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SessionResponse findById(Long id) {
        return toResponse(visibleSession(id));
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> findByFormation(Long formationId) {
        formationService.findById(formationId);
        return sessionRepository.findByFormationIdWithFormation(formationId).stream()
                .filter(this::visibleToCaller)
                .map(this::toResponse)
                .toList();
    }

    public SessionResponse create(SessionRequest request) {
        TrainingSession session = new TrainingSession();
        apply(session, request, true);
        return toResponse(sessionRepository.save(session));
    }

    public SessionResponse update(Long id, SessionRequest request) {
        TrainingSession session = sessionRepository.findByIdWithFormation(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Session introuvable"));
        apply(session, request, false);
        return toResponse(session);
    }

    public void delete(Long id) {
        TrainingSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Session introuvable"));
        if (enrollmentRepository.existsBySessionId(id)) {
            throw new ApiException(HttpStatus.CONFLICT, "Impossible de supprimer une session qui a des inscriptions");
        }
        sessionRepository.delete(session);
    }

    TrainingSession visibleSession(Long id) {
        TrainingSession session = sessionRepository.findByIdWithFormation(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Session introuvable"));
        if (!visibleToCaller(session)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Session introuvable");
        }
        return session;
    }

    private boolean visibleToCaller(TrainingSession session) {
        return SecurityUtils.isAdmin() || session.getFormation().getStatus() == FormationStatus.PUBLISHED;
    }

    private void apply(TrainingSession session, SessionRequest request, boolean creating) {
        Formation formation = formationRepository.findByIdWithCategory(request.getFormationId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "La formation est obligatoire et doit exister"));
        long active = creating ? 0 : enrollmentRepository.countActiveBySessionId(session.getId());
        if (request.getCapacity() < active) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "La capacité ne peut pas être inférieure au nombre d'inscriptions actives (" + active + ")");
        }
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La date de fin doit être postérieure ou égale à la date de début");
        }
        session.setFormation(formation);
        session.setStartDate(request.getStartDate());
        session.setEndDate(request.getEndDate());
        session.setCapacity(request.getCapacity());
        session.setStatus(request.getStatus());
    }

    private SessionResponse toResponse(TrainingSession session) {
        return SessionMapper.toResponse(session, enrollmentRepository.countActiveBySessionId(session.getId()));
    }
}
