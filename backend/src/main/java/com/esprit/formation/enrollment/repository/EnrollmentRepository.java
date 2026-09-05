package com.esprit.formation.enrollment.repository;

import com.esprit.formation.enrollment.domain.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByUserIdAndSessionId(Long userId, Long sessionId);

    boolean existsBySessionId(Long sessionId);

    @Query("""
            SELECT COUNT(e) FROM Enrollment e
            WHERE e.session.id = :sessionId
              AND e.status <> :cancelled
            """)
    long countActiveBySessionId(
            @Param("sessionId") Long sessionId,
            @Param("cancelled") com.esprit.formation.common.domain.EnrollmentStatus cancelled
    );

    default long countActiveBySessionId(Long sessionId) {
        return countActiveBySessionId(sessionId, com.esprit.formation.common.domain.EnrollmentStatus.CANCELLED);
    }

    @Query("""
            SELECT e FROM Enrollment e
            JOIN FETCH e.user
            JOIN FETCH e.session s
            JOIN FETCH s.formation f
            JOIN FETCH f.category
            WHERE e.user.id = :userId
            ORDER BY e.createdAt DESC
            """)
    List<Enrollment> findByUserIdWithDetails(@Param("userId") Long userId);

    @Query("""
            SELECT e FROM Enrollment e
            JOIN FETCH e.user
            JOIN FETCH e.session s
            JOIN FETCH s.formation f
            JOIN FETCH f.category
            WHERE e.session.id = :sessionId
            ORDER BY e.createdAt DESC
            """)
    List<Enrollment> findBySessionIdWithDetails(@Param("sessionId") Long sessionId);

    @Query("""
            SELECT e FROM Enrollment e
            JOIN FETCH e.user
            JOIN FETCH e.session s
            JOIN FETCH s.formation f
            JOIN FETCH f.category
            WHERE e.id = :id
            """)
    Optional<Enrollment> findByIdWithDetails(@Param("id") Long id);
}
