package com.esprit.formation.catalogue.dto;

import com.esprit.formation.common.domain.FormationStatus;

import java.math.BigDecimal;
import java.time.Instant;

public class FormationResponse {

    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private Integer durationHours;
    private CategoryResponse category;
    private FormationStatus status;
    private Instant createdAt;
    private long chapterCount;

    public FormationResponse(Long id, String title, String description, BigDecimal price, Integer durationHours,
                             CategoryResponse category, FormationStatus status, Instant createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.durationHours = durationHours;
        this.category = category;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getDurationHours() {
        return durationHours;
    }

    public CategoryResponse getCategory() {
        return category;
    }

    public FormationStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public long getChapterCount() {
        return chapterCount;
    }

    public void setChapterCount(long chapterCount) {
        this.chapterCount = chapterCount;
    }
}
