package com.esprit.formation.catalogue.repository;

import com.esprit.formation.catalogue.domain.Formation;
import com.esprit.formation.common.domain.FormationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface FormationRepository extends JpaRepository<Formation, Long> {

    Optional<Formation> findFirstByTitle(String title);

    boolean existsByCategoryId(Long categoryId);

    long countByCategoryId(Long categoryId);


    @Query("""
            SELECT f FROM Formation f JOIN FETCH f.category
            WHERE f.status = :status
            ORDER BY f.createdAt DESC
            """)
    List<Formation> findAllByStatusWithCategory(@Param("status") FormationStatus status);

    @Query("""
            SELECT f FROM Formation f JOIN FETCH f.category
            WHERE f.id = :id
            """)
    Optional<Formation> findByIdWithCategory(@Param("id") Long id);


    @Query("""
            SELECT DISTINCT f FROM Formation f JOIN FETCH f.category
            WHERE (:publishedOnly = false OR f.status = :published)
              AND (:categoryId IS NULL OR f.category.id = :categoryId)
              AND (:minPrice IS NULL OR f.price >= :minPrice)
              AND (:maxPrice IS NULL OR f.price <= :maxPrice)
              AND (:q IS NULL OR LOWER(f.title) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(f.description) LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY f.createdAt DESC
            """)
    List<Formation> search(
            @Param("publishedOnly") boolean publishedOnly,
            @Param("published") FormationStatus published,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("q") String q
    );
}
