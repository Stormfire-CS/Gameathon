package edu.carroll.cs341.service;

import edu.carroll.cs341.jpa.model.GamesOwned;

import java.util.List;

public interface GamesOwnedService {

    List<GamesOwned> getGamesOwnedByOwner(Long ownerID);
}
