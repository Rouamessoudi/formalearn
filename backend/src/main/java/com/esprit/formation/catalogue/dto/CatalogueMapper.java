package com.esprit.formation.catalogue.dto;

import com.esprit.formation.catalogue.domain.Category;
import com.esprit.formation.catalogue.domain.Chapter;
import com.esprit.formation.catalogue.domain.Formation;

public final class CatalogueMapper {

    private CatalogueMapper() {
    }

    public static CategoryResponse toCategory(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription());
    }

    public static FormationResponse toFormation(Formation formation) {
        return new FormationResponse(
                formation.getId(),
                formation.getTitle(),
                formation.getDescription(),
                formation.getPrice(),
                formation.getDurationHours(),
                toCategory(formation.getCategory()),
                formation.getStatus(),
                formation.getCreatedAt()
        );
    }

    public static ChapterResponse toChapter(Chapter chapter) {
        return new ChapterResponse(chapter.getId(), chapter.getTitle(), chapter.getContent(), chapter.getPosition());
    }
}
