package edu.carroll.cs341.service;

import edu.carroll.cs341.jpa.model.Game;

import java.util.List;

public interface GamesService {
    List<Game> getAvailableGames(Long ownerID);
    void addGame(String username, String gameName, Integer minPlayers, Integer maxPlayers);
}
