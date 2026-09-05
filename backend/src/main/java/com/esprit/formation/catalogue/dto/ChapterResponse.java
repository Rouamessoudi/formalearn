package com.esprit.formation.catalogue.dto;

public class ChapterResponse {

    private Long id;
    private String title;
    private String content;
    private Integer position;

    public ChapterResponse(Long id, String title, String content, Integer position) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.position = position;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public Integer getPosition() {
        return position;
    }
}
