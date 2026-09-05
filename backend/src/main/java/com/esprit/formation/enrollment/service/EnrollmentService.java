package com.esprit.formation.enrollment.service;

import com.esprit.formation.catalogue.domain.Formation;
import com.esprit.formation.common.domain.EnrollmentStatus;
import com.esprit.formation.common.domain.FormationStatus;
import com.esprit.formation.common.domain.SessionStatus;
import com.esprit.formation.common.exception.ApiException;
import com.esprit.formation.enrollment.domain.Enrollment;
import com.esprit.formation.enrollment.dto.EnrollmentMapper;
import com.esprit.formation.enrollment.dto.EnrollmentRequest;
import com.esprit.formation.enrollment.dto.EnrollmentResponse;
import com.esprit.formation.enrollment.dto.EnrollmentStatusRequest;
import com.esprit.formation.enrollment.repository.EnrollmentRepository;
import com.esprit.formation.security.SecurityUtils;
import com.esprit.formation.session.domain.TrainingSession;
import com.esprit.formation.session.repository.TrainingSessionRepository;
import com.esprit.formation.session.service.SessionService;
import com.esprit.formation.user.domain.User;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
@Transactional
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final TrainingSessionRepository sessionRepository;
    private final SessionService sessionService;

    public EnrollmentService(
            EnrollmentRepository enrollmentRepository,
            TrainingSessionRepository sessionRepository,
            SessionService sessionService
    ) {
        this.enrollmentRepository = enrollmentRepository;
        this.sessionRepository = sessionRepository;
        this.sessionService = sessionService;
    }

    public EnrollmentResponse enroll(EnrollmentRequest request) {
        User learner = SecurityUtils.requireUser();
        TrainingSession session = sessionRepository.findByIdForUpdate(request.getSessionId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Session introuvable"));

        Formation formation = session.getFormation();
        if (formation.getStatus() != FormationStatus.PUBLISHED) {
            throw new ApiException(HttpStatus.CONFLICT, "Impossible de s'inscrire à une formation non publiée");
        }
        if (session.getStatus() != SessionStatus.OPEN) {
            throw new ApiException(HttpStatus.CONFLICT, "Cette session est fermée, les inscriptions ne sont plus possibles");
        }
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        if (session.getStartDate().isBefore(today)) {
            throw new ApiException(HttpStatus.CONFLICT, "Les inscriptions sont closes : la date de début est déjà passée");
        }
        if (enrollmentRepository.existsByUserIdAndSessionId(learner.getId(), session.getId())) {
            throw new ApiException(HttpStatus.CONFLICT, "Vous êtes déjà inscrit à cette session");
        }
        long active = enrollmentRepository.countActiveBySessionId(session.getId());
        if (active >= session.getCapacity()) {
            throw new ApiException(HttpStatus.CONFLICT, "Cette session est complète, il n'y a plus de places disponibles");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setUser(learner);
        enrollment.setSession(session);
        enrollment.setStatus(EnrollmentStatus.PENDING);
        try {
            Enrollment saved = enrollmentRepository.saveAndFlush(enrollment);
            return decorate(EnrollmentMapper.toResponse(saved));
        } catch (DataIntegrityViolationException ex) {
            throw new ApiException(HttpStatus.CONFLICT, "Vous êtes déjà inscrit à cette session");
        }
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> myEnrollments() {
        User learner = SecurityUtils.requireUser();
        return enrollmentRepository.findByUserIdWithDetails(learner.getId()).stream()
                .map(item -> decorate(EnrollmentMapper.toResponse(item)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> listBySession(Long sessionId) {
        sessionService.findById(sessionId);
        return enrollmentRepository.findBySessionIdWithDetails(sessionId).stream()
                .map(item -> decorate(EnrollmentMapper.toResponse(item)))
                .toList();
    }

    public EnrollmentResponse updateStatus(Long id, EnrollmentStatusRequest request) {
        Enrollment enrollment = enrollmentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Inscription introuvable"));
        EnrollmentStatus current = enrollment.getStatus();
        EnrollmentStatus next = request.getStatus();
        if (current == next) {
            return decorate(EnrollmentMapper.toResponse(enrollment));
        }
        boolean confirm = current == EnrollmentStatus.PENDING && next == EnrollmentStatus.CONFIRMED;
        boolean cancel = (current == EnrollmentStatus.PENDING || current == EnrollmentStatus.CONFIRMED)
                && next == EnrollmentStatus.CANCELLED;
        if (!confirm && !cancel) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Transition de statut non autorisée. Autorisé : PENDING → CONFIRMED, ou PENDING/CONFIRMED → CANCELLED");
        }
        enrollment.setStatus(next);
        return decorate(EnrollmentMapper.toResponse(enrollment));
    }

    public EnrollmentResponse cancelMine(Long id) {
        User learner = SecurityUtils.requireUser();
        Enrollment enrollment = enrollmentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Inscription introuvable"));
        if (!enrollment.getUser().getId().equals(learner.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Vous ne pouvez annuler que votre propre inscription");
        }
        EnrollmentStatusRequest request = new EnrollmentStatusRequest();
        request.setStatus(EnrollmentStatus.CANCELLED);
        return updateStatus(id, request);
    }

    private EnrollmentResponse decorate(EnrollmentResponse response) {
        long active = enrollmentRepository.countActiveBySessionId(response.getSessionId());
        Integer capacity = response.getCapacity();
        if (capacity == null) {
            TrainingSession session = sessionRepository.findById(response.getSessionId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Session introuvable"));
            capacity = session.getCapacity();
            response.setCapacity(capacity);
        }
        response.setEnrolledCount(active);
        response.setRemainingPlaces(Math.max(0, capacity - active));
        return response;
    }
}
