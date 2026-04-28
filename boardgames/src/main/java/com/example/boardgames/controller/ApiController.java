package com.example.boardgames.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.boardgames.model.BoardGame;
import com.example.boardgames.repository.BoardGameRepository;
import com.example.boardgames.repository.CategoryRepository;
import com.example.boardgames.serializer.BoardGameSerializer;
import com.example.boardgames.service.CentrifugoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Аналог views.py в Django — содержит вьюшки (Generic Views) для модели BoardGame
@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private BoardGameRepository gameRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CentrifugoService centrifugoService;

    // --- Поиск (HW5) ---

    // GET /api/search?q=...  — ищет по title и description
    @GetMapping("/search")
    public ResponseEntity<List<BoardGameSerializer>> searchGames(@RequestParam(name = "q") String q) {
        List<BoardGameSerializer> results = gameRepository
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(q, q)
                .stream()
                .map(BoardGameSerializer::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(results);
    }

    // --- BoardGame Generic Views (аналог ListCreateAPIView + RetrieveUpdateDestroyAPIView) ---

    // GET /api/games/  — список всех игр (аналог DRF ListAPIView)
    @GetMapping("/games/")
    public ResponseEntity<List<BoardGameSerializer>> getAllGames() {
        List<BoardGameSerializer> games = gameRepository.findAll()
                .stream()
                .map(BoardGameSerializer::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(games);
    }

    // POST /api/games/create  — создание новой игры (аналог DRF CreateAPIView)
    @PostMapping("/games/create")
    public ResponseEntity<BoardGameSerializer> createGame(@RequestBody BoardGameSerializer data) {
        BoardGame game = new BoardGame();
        game.setTitle(data.getTitle());
        game.setDescription(data.getDescription());
        if (data.getCategoryId() != null) {
            categoryRepository.findById(data.getCategoryId()).ifPresent(game::setCategory);
        }
        BoardGameSerializer result = BoardGameSerializer.fromEntity(gameRepository.save(game));
        centrifugoService.publish("games", result);
        return ResponseEntity.ok(result);
    }

    // GET /api/games/{id}  — детальное описание игры (аналог DRF RetrieveAPIView)
    @GetMapping("/games/{id}")
    public ResponseEntity<?> getGame(@PathVariable Long id) {
        BoardGame game = gameRepository.findById(id).orElse(null);
        if (game == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(BoardGameSerializer.fromEntity(game));
    }

    // PUT /api/games/{id}  — изменение игры (аналог DRF UpdateAPIView)
    @PutMapping("/games/{id}")
    public ResponseEntity<?> updateGame(@PathVariable Long id, @RequestBody BoardGameSerializer data) {
        BoardGame game = gameRepository.findById(id).orElse(null);
        if (game == null) return ResponseEntity.notFound().build();
        if (data.getTitle() != null) game.setTitle(data.getTitle());
        if (data.getDescription() != null) game.setDescription(data.getDescription());
        if (data.getCategoryId() != null) {
            categoryRepository.findById(data.getCategoryId()).ifPresent(game::setCategory);
        }
        return ResponseEntity.ok(BoardGameSerializer.fromEntity(gameRepository.save(game)));
    }

    // DELETE /api/games/{id}  — удаление игры (аналог DRF DestroyAPIView)
    @DeleteMapping("/games/{id}")
    public ResponseEntity<Void> deleteGame(@PathVariable Long id) {
        if (!gameRepository.existsById(id)) return ResponseEntity.notFound().build();
        gameRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // --- Стабы из HW3 (оставлены для демонстрации) ---

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

    @PostMapping("/favorites/{gameId}")
    public Map<String, Object> addToFavorites(@PathVariable Long gameId) {
        return Map.of("message", "Игра " + gameId + " добавлена в избранное!");
    }
}