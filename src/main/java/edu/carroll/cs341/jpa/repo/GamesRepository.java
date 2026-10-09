package edu.carroll.cs341.jpa.repo;

import edu.carroll.cs341.jpa.model.Games;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GamesRepository extends JpaRepository<Games, Long> {

    boolean existsByGameName(String gameName);

    Games findByGameNameIgnoreCase(String gameName);

    Games findByGameID(Long gameID);

    List<Games> findAllByOrderByGameNameAsc();
}
