package com.esprit.formation.catalogue.web;

import com.esprit.formation.catalogue.dto.FormationRequest;
import com.esprit.formation.catalogue.dto.FormationResponse;
import com.esprit.formation.catalogue.service.FormationService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/formations")
public class FormationController {

    private final FormationService formationService;

    public FormationController(FormationService formationService) {
        this.formationService = formationService;
    }

    @GetMapping
    public List<FormationResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice
    ) {
        return formationService.search(q, categoryId, minPrice, maxPrice);
    }

    @GetMapping("/{id}")
    public FormationResponse findById(@PathVariable Long id) {
        return formationService.findById(id);
    }

    @PostMapping
    public ResponseEntity<FormationResponse> create(@Valid @RequestBody FormationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(formationService.create(request));
    }

    @PutMapping("/{id}")
    public FormationResponse update(@PathVariable Long id, @Valid @RequestBody FormationRequest request) {
        return formationService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        formationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
