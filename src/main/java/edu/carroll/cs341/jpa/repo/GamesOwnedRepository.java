package edu.carroll.cs341.jpa.repo;

import edu.carroll.cs341.jpa.model.GamesOwned;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GamesOwnedRepository extends JpaRepository<GamesOwned, Long> {

    List<GamesOwned> findByOwnerID(Long ownerID);
}
