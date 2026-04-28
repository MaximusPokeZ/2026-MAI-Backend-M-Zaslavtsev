package com.example.boardgames.controller;

import java.util.List;
import java.util.stream.Collectors;

import com.example.boardgames.model.Category;
import com.example.boardgames.repository.BoardGameRepository;
import com.example.boardgames.repository.CategoryRepository;
import com.example.boardgames.serializer.CategorySerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Аналог views.py в Django — содержит вьюшки (Generic Views) для модели Category
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BoardGameRepository gameRepository;

    // GET /api/categories/  — список всех категорий (аналог DRF ListAPIView)
    @GetMapping("/")
    public ResponseEntity<List<CategorySerializer>> getAllCategories() {
        List<CategorySerializer> categories = categoryRepository.findAll()
                .stream()
                .map(CategorySerializer::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }

    // POST /api/categories/  — создание категории (аналог DRF CreateAPIView)
    @PostMapping("/")
    public ResponseEntity<CategorySerializer> createCategory(@RequestBody CategorySerializer data) {
        Category cat = new Category();
        cat.setName(data.getName());
        return ResponseEntity.ok(CategorySerializer.fromEntity(categoryRepository.save(cat)));
    }

    // GET /api/categories/{id}  — детальное описание (аналог DRF RetrieveAPIView)
    @GetMapping("/{id}")
    public ResponseEntity<?> getCategory(@PathVariable Long id) {
        Category cat = categoryRepository.findById(id).orElse(null);
        if (cat == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(CategorySerializer.fromEntity(cat));
    }

    // PUT /api/categories/{id}  — изменение категории (аналог DRF UpdateAPIView)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody CategorySerializer data) {
        Category cat = categoryRepository.findById(id).orElse(null);
        if (cat == null) return ResponseEntity.notFound().build();
        if (data.getName() != null) cat.setName(data.getName());
        return ResponseEntity.ok(CategorySerializer.fromEntity(categoryRepository.save(cat)));
    }

    // DELETE /api/categories/{id}  — удаление категории (аналог DRF DestroyAPIView)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        if (!categoryRepository.existsById(id)) return ResponseEntity.notFound().build();
        // Обнуляем category у связанных игр, иначе нарушение внешнего ключа
        gameRepository.findByCategory_Id(id).forEach(game -> {
            game.setCategory(null);
            gameRepository.save(game);
        });
        categoryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}