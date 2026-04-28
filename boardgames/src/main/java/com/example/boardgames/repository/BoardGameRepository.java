package com.example.boardgames.repository;

import java.util.List;

import com.example.boardgames.model.BoardGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardGameRepository extends JpaRepository<BoardGame, Long> {
    List<BoardGame> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description);

    List<BoardGame> findByCategory_Id(Long categoryId);
}