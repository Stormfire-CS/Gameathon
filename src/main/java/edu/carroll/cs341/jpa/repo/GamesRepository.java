package edu.carroll.cs341.jpa.repo;

import edu.carroll.cs341.jpa.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GamesRepository extends JpaRepository<Game, Long> {

    boolean existsByGameName(String gameName);

    Game findByGameNameIgnoreCase(String gameName);

    Game findByGameID(Long gameID);

    List<Game> findByOwnerIDOfAdderIsNullOrOwnerIDOfAdderOrderByGameNameAsc(Long ownerIDOfAdder);
}
