package edu.carroll.cs341.service;

import edu.carroll.cs341.jpa.model.GameOwned;

import java.util.List;

public interface GamesOwnedService {

    List<GameOwned> getGamesOwnedByUsername(String username);

    void addGame(String username, Long gameID, Integer yearProduced);
}
