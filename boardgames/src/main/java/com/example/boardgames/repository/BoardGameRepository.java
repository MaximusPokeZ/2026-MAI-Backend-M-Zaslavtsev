package com.example.boardgames.repository;

import java.util.List;

import com.example.boardgames.model.BoardGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardGameRepository extends JpaRepository<BoardGame, Long> {
    // Ищет вхождение строки q в названии (Title) ИЛИ в описании (Description), игнорируя регистр
    List<BoardGame> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description);
}