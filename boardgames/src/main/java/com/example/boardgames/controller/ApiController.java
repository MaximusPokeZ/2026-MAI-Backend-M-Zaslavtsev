package com.example.boardgames.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {

    @GetMapping("/profile")
    public Map<String, Object> getProfile() {
        return Map.of("name", "Max");
    }

    @GetMapping("/games")
    public Map<String, Object> getGamesList() {
        Map<String, Object> response = new HashMap<>();
        response.put("total", 2);
        response.put("items", List.of(
                Map.of("id", 1, "title", "Дурак", "category", "кард-гейм"),
                Map.of("id", 2, "title", "Монополия", "category", "Пати-гейм")
        ));
        return response;
    }

    @GetMapping("/categories/{id}")
    public Map<String, Object> getCategory(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        response.put("category_id", id);

        if (id == 1L) {
            response.put("name", "Стратегии");
            response.put("description", "Игры, где нужно думать");
        } else {
            response.put("name", "Другая категория");
            response.put("description", "Описание для категории " + id);
        }

        return response;
    }

    @PostMapping("/favorites/{gameId}")
    public Map<String, Object> addToFavorites(@PathVariable Long gameId) {
        Map<String, Object> response = new HashMap<>();

        response.put("message", "Игра " + gameId + " добавлена в избранное!");
        return response;
    }
}
