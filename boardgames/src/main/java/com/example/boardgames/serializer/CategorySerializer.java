package com.example.boardgames.serializer;

import com.example.boardgames.model.Category;

// Аналог DRF ModelSerializer — отвечает за сериализацию/десериализацию Category в JSON
public class CategorySerializer {
    private Long id;
    private String name;

    public static CategorySerializer fromEntity(Category category) {
        CategorySerializer s = new CategorySerializer();
        s.id = category.getId();
        s.name = category.getName();
        return s;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}