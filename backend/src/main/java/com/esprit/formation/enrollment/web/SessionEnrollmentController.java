package com.esprit.formation.enrollment.web;

import com.esprit.formation.enrollment.dto.EnrollmentResponse;
import com.esprit.formation.enrollment.service.EnrollmentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sessions/{sessionId}/inscriptions")
public class SessionEnrollmentController {

    private final EnrollmentService enrollmentService;

    public SessionEnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping
    public List<EnrollmentResponse> list(@PathVariable Long sessionId) {
        return enrollmentService.listBySession(sessionId);
    }
}
