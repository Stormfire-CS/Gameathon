package edu.carroll.cs341.jpa.repo;

import edu.carroll.cs341.jpa.model.GameOwned;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GamesOwnedRepository extends JpaRepository<GameOwned, Long> {

    List<GameOwned> findByOwnerID(Long ownerID);
}
