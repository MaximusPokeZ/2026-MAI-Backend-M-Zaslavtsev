package com.example.boardgames.serializer;

import com.example.boardgames.model.BoardGame;

// Аналог DRF ModelSerializer — отвечает за сериализацию/десериализацию BoardGame в JSON
public class BoardGameSerializer {
    private Long id;
    private String title;
    private String description;
    private Long categoryId;
    private String categoryName;

    public static BoardGameSerializer fromEntity(BoardGame game) {
        BoardGameSerializer s = new BoardGameSerializer();
        s.id = game.getId();
        s.title = game.getTitle();
        s.description = game.getDescription();
        if (game.getCategory() != null) {
            s.categoryId = game.getCategory().getId();
            s.categoryName = game.getCategory().getName();
        }
        return s;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
}