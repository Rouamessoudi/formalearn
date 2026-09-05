package com.esprit.formation.session.web;

import com.esprit.formation.session.dto.SessionRequest;
import com.esprit.formation.session.dto.SessionResponse;
import com.esprit.formation.session.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping
    public List<SessionResponse> findAll() {
        return sessionService.findAll();
    }

    @GetMapping("/{id}")
    public SessionResponse findById(@PathVariable Long id) {
        return sessionService.findById(id);
    }

    @PostMapping
    public ResponseEntity<SessionResponse> create(@Valid @RequestBody SessionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sessionService.create(request));
    }

    @PutMapping("/{id}")
    public SessionResponse update(@PathVariable Long id, @Valid @RequestBody SessionRequest request) {
        return sessionService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sessionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
