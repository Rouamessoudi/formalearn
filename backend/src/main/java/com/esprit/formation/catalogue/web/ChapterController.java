package com.esprit.formation.catalogue.web;

import com.esprit.formation.catalogue.dto.ChapterRequest;
import com.esprit.formation.catalogue.dto.ChapterResponse;
import com.esprit.formation.catalogue.service.ChapterService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/formations/{formationId}/chapitres")
public class ChapterController {

    private final ChapterService chapterService;

    public ChapterController(ChapterService chapterService) {
        this.chapterService = chapterService;
    }

    @GetMapping
    public List<ChapterResponse> list(@PathVariable Long formationId) {
        return chapterService.list(formationId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ChapterResponse> create(
            @PathVariable Long formationId,
            @Valid @RequestBody ChapterRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chapterService.create(formationId, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{chapterId}")
    public ChapterResponse update(
            @PathVariable Long formationId,
            @PathVariable Long chapterId,
            @Valid @RequestBody ChapterRequest request
    ) {
        return chapterService.update(formationId, chapterId, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{chapterId}/position")
    public List<ChapterResponse> move(
            @PathVariable Long formationId,
            @PathVariable Long chapterId,
            @RequestParam String direction
    ) {
        return chapterService.move(formationId, chapterId, direction);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{chapterId}")
    public ResponseEntity<Void> delete(@PathVariable Long formationId, @PathVariable Long chapterId) {
        chapterService.delete(formationId, chapterId);
        return ResponseEntity.noContent().build();
    }
}
