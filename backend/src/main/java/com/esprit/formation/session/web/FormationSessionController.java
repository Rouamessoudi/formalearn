package com.esprit.formation.session.web;

import com.esprit.formation.session.dto.SessionResponse;
import com.esprit.formation.session.service.SessionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/formations/{formationId}/sessions")
public class FormationSessionController {

    private final SessionService sessionService;

    public FormationSessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping
    public List<SessionResponse> list(@PathVariable Long formationId) {
        return sessionService.findByFormation(formationId);
    }
}
