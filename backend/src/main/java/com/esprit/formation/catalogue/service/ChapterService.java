package com.esprit.formation.catalogue.service;

import com.esprit.formation.catalogue.domain.Chapter;
import com.esprit.formation.catalogue.domain.Formation;
import com.esprit.formation.catalogue.dto.CatalogueMapper;
import com.esprit.formation.catalogue.dto.ChapterRequest;
import com.esprit.formation.catalogue.dto.ChapterResponse;
import com.esprit.formation.catalogue.repository.ChapterRepository;
import com.esprit.formation.common.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ChapterService {

    private final ChapterRepository chapterRepository;
    private final FormationService formationService;

    public ChapterService(ChapterRepository chapterRepository, FormationService formationService) {
        this.chapterRepository = chapterRepository;
        this.formationService = formationService;
    }

    @Transactional(readOnly = true)
    public List<ChapterResponse> list(Long formationId) {
        Formation formation = formationService.visibleFormation(formationId);
        return chapterRepository.findByFormationIdOrderByPositionAsc(formation.getId()).stream()
                .map(CatalogueMapper::toChapter)
                .toList();
    }

    public ChapterResponse create(Long formationId, ChapterRequest request) {
        Formation formation = formationService.visibleFormation(formationId);
        if (chapterRepository.existsByFormationIdAndPosition(formationId, request.getPosition())) {
            throw new ApiException(HttpStatus.CONFLICT, "Un chapitre utilise déjà cette position");
        }
        Chapter chapter = new Chapter();
        chapter.setFormation(formation);
        chapter.setTitle(request.getTitle().trim());
        chapter.setContent(request.getContent());
        chapter.setPosition(request.getPosition());
        return CatalogueMapper.toChapter(chapterRepository.save(chapter));
    }

    public ChapterResponse update(Long formationId, Long chapterId, ChapterRequest request) {
        formationService.visibleFormation(formationId);
        Chapter chapter = chapterRepository.findByIdAndFormationId(chapterId, formationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Chapitre introuvable"));
        if (chapterRepository.existsByFormationIdAndPositionAndIdNot(formationId, request.getPosition(), chapterId)) {
            throw new ApiException(HttpStatus.CONFLICT, "Un chapitre utilise déjà cette position");
        }
        chapter.setTitle(request.getTitle().trim());
        chapter.setContent(request.getContent());
        chapter.setPosition(request.getPosition());
        return CatalogueMapper.toChapter(chapter);
    }

    public void delete(Long formationId, Long chapterId) {
        formationService.visibleFormation(formationId);
        Chapter chapter = chapterRepository.findByIdAndFormationId(chapterId, formationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Chapitre introuvable"));
        chapterRepository.delete(chapter);
    }

    public List<ChapterResponse> move(Long formationId, Long chapterId, String direction) {
        formationService.visibleFormation(formationId);
        int delta = "up".equalsIgnoreCase(direction) ? -1 : 1;
        if (!"up".equalsIgnoreCase(direction) && !"down".equalsIgnoreCase(direction)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "direction doit être up ou down");
        }
        List<Chapter> ordered = chapterRepository.findByFormationIdOrderByPositionAsc(formationId);
        int index = -1;
        for (int i = 0; i < ordered.size(); i++) {
            if (ordered.get(i).getId().equals(chapterId)) {
                index = i;
                break;
            }
        }
        if (index < 0) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Chapitre introuvable");
        }
        int target = index + delta;
        if (target < 0 || target >= ordered.size()) {
            return ordered.stream().map(CatalogueMapper::toChapter).toList();
        }
        Chapter first = ordered.get(index);
        Chapter second = ordered.get(target);
        int posFirst = first.getPosition();
        int posSecond = second.getPosition();
        first.setPosition(-Math.toIntExact(first.getId()));
        chapterRepository.saveAndFlush(first);
        second.setPosition(posFirst);
        chapterRepository.saveAndFlush(second);
        first.setPosition(posSecond);
        chapterRepository.saveAndFlush(first);
        return chapterRepository.findByFormationIdOrderByPositionAsc(formationId).stream()
                .map(CatalogueMapper::toChapter)
                .toList();
    }
}
