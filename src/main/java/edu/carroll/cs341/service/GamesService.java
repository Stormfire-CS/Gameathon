package edu.carroll.cs341.service;

import edu.carroll.cs341.jpa.model.Games;

import java.util.List;

public interface GamesService {
    List<Games> getAvailableGames(Long ownerID);
    void addGame(String username, String gameName, Integer minPlayers, Integer maxPlayers);
}
