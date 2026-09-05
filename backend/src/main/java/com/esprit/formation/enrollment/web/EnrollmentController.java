package com.esprit.formation.enrollment.web;

import com.esprit.formation.enrollment.dto.EnrollmentRequest;
import com.esprit.formation.enrollment.dto.EnrollmentResponse;
import com.esprit.formation.enrollment.dto.EnrollmentStatusRequest;
import com.esprit.formation.enrollment.service.EnrollmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inscriptions")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping
    public ResponseEntity<EnrollmentResponse> enroll(@Valid @RequestBody EnrollmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollmentService.enroll(request));
    }

    @GetMapping("/moi")
    public List<EnrollmentResponse> mine() {
        return enrollmentService.myEnrollments();
    }

    @PostMapping("/{id}/annuler")
    public EnrollmentResponse cancelMine(@PathVariable Long id) {
        return enrollmentService.cancelMine(id);
    }

    @PatchMapping("/{id}/status")
    public EnrollmentResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody EnrollmentStatusRequest request
    ) {
        return enrollmentService.updateStatus(id, request);
    }
}
