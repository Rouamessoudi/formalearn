package com.esprit.formation.session.repository;

import com.esprit.formation.session.domain.TrainingSession;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrainingSessionRepository extends JpaRepository<TrainingSession, Long> {

    boolean existsByFormationId(Long formationId);

    boolean existsByFormationIdAndStartDate(Long formationId, LocalDate startDate);

    @Query("""
            SELECT s FROM TrainingSession s
            JOIN FETCH s.formation f
            JOIN FETCH f.category
            ORDER BY s.startDate ASC
            """)
    List<TrainingSession> findAllWithFormation();

    @Query("""
            SELECT s FROM TrainingSession s
            JOIN FETCH s.formation f
            JOIN FETCH f.category
            WHERE s.id = :id
            """)
    Optional<TrainingSession> findByIdWithFormation(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT s FROM TrainingSession s
            JOIN FETCH s.formation f
            JOIN FETCH f.category
            WHERE s.id = :id
            """)
    Optional<TrainingSession> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            SELECT s FROM TrainingSession s
            JOIN FETCH s.formation f
            JOIN FETCH f.category
            WHERE f.id = :formationId
            ORDER BY s.startDate ASC
            """)
    List<TrainingSession> findByFormationIdWithFormation(@Param("formationId") Long formationId);
}
