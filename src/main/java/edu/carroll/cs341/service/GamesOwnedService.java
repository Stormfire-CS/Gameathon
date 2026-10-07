package edu.carroll.cs341.service;

import edu.carroll.cs341.jpa.model.Games;
import edu.carroll.cs341.jpa.model.GamesOwned;

import java.util.List;

public interface GamesOwnedService {

    List<GamesOwned> getGamesOwnedByUsername(String username);

    void addGame(String username, String gameName, Integer yearProduced);

    List<Games> findGames(String gameName);
}
