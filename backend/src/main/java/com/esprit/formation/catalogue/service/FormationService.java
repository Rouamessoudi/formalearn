package com.esprit.formation.catalogue.service;

import com.esprit.formation.catalogue.domain.Category;
import com.esprit.formation.catalogue.domain.Formation;
import com.esprit.formation.catalogue.dto.CatalogueMapper;
import com.esprit.formation.catalogue.dto.FormationRequest;
import com.esprit.formation.catalogue.dto.FormationResponse;
import com.esprit.formation.catalogue.repository.CategoryRepository;
import com.esprit.formation.catalogue.repository.ChapterRepository;
import com.esprit.formation.catalogue.repository.FormationRepository;
import com.esprit.formation.session.repository.TrainingSessionRepository;
import com.esprit.formation.common.domain.FormationStatus;
import com.esprit.formation.common.exception.ApiException;
import com.esprit.formation.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class FormationService {

    private final FormationRepository formationRepository;
    private final CategoryRepository categoryRepository;
    private final TrainingSessionRepository sessionRepository;
    private final ChapterRepository chapterRepository;

    public FormationService(
            FormationRepository formationRepository,
            CategoryRepository categoryRepository,
            TrainingSessionRepository sessionRepository,
            ChapterRepository chapterRepository
    ) {
        this.formationRepository = formationRepository;
        this.categoryRepository = categoryRepository;
        this.sessionRepository = sessionRepository;
        this.chapterRepository = chapterRepository;
    }

    @Transactional(readOnly = true)
    public List<FormationResponse> search(String q, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice) {
        String query = (q == null || q.isBlank()) ? null : q.trim();
        boolean publishedOnly = !SecurityUtils.isAdmin();
        return formationRepository.search(publishedOnly, FormationStatus.PUBLISHED, categoryId, minPrice, maxPrice, query)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FormationResponse findById(Long id) {
        return toResponse(visibleFormation(id));
    }

    public FormationResponse create(FormationRequest request) {
        Formation formation = new Formation();
        apply(formation, request);
        return toResponse(formationRepository.save(formation));
    }

    public FormationResponse update(Long id, FormationRequest request) {
        Formation formation = formationRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Formation introuvable"));
        apply(formation, request);
        return toResponse(formation);
    }

    public void delete(Long id) {
        Formation formation = formationRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Formation introuvable"));
        if (sessionRepository.existsByFormationId(id)) {
            throw new ApiException(HttpStatus.CONFLICT, "Impossible de supprimer une formation qui a des sessions");
        }
        formationRepository.delete(formation);
    }

    Formation visibleFormation(Long id) {
        Formation formation = formationRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Formation introuvable"));
        if (!SecurityUtils.isAdmin() && formation.getStatus() != FormationStatus.PUBLISHED) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Formation introuvable");
        }
        return formation;
    }

    private void apply(Formation formation, FormationRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "La catégorie est obligatoire et doit exister"));
        formation.setTitle(request.getTitle().trim());
        formation.setDescription(request.getDescription().trim());
        formation.setPrice(request.getPrice());
        formation.setDurationHours(request.getDurationHours());
        formation.setCategory(category);
        formation.setStatus(request.getStatus());
    }

    private FormationResponse toResponse(Formation formation) {
        FormationResponse response = CatalogueMapper.toFormation(formation);
        response.setChapterCount(chapterRepository.countByFormationId(formation.getId()));
        return response;
    }
}
