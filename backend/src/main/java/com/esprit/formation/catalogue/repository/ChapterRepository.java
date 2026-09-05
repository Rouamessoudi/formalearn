package com.esprit.formation.catalogue.repository;

import com.esprit.formation.catalogue.domain.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    List<Chapter> findByFormationIdOrderByPositionAsc(Long formationId);

    Optional<Chapter> findByIdAndFormationId(Long id, Long formationId);

    Optional<Chapter> findByFormationIdAndPosition(Long formationId, Integer position);

    boolean existsByFormationIdAndPosition(Long formationId, Integer position);

    boolean existsByFormationIdAndPositionAndIdNot(Long formationId, Integer position, Long id);

    long countByFormationId(Long formationId);
}
