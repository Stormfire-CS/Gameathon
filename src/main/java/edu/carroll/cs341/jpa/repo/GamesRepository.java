package edu.carroll.cs341.jpa.repo;

import edu.carroll.cs341.jpa.model.Games;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GamesRepository extends JpaRepository<Games, Long> {

    boolean existsByGameName(String gameName);
}
