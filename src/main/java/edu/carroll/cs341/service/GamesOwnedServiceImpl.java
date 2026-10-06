package edu.carroll.cs341.service;

import edu.carroll.cs341.jpa.model.GamesOwned;
import edu.carroll.cs341.jpa.repo.GamesOwnedRepository;
import edu.carroll.cs341.jpa.repo.GamesRepository;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class GamesOwnedServiceImpl implements GamesOwnedService {

    private final GamesOwnedRepository gamesOwnedRepo;
    private final GamesRepository gamesRepo;

    public GamesOwnedServiceImpl(GamesOwnedRepository gamesOwnedRepo, GamesRepository gamesRepo) {
        this.gamesOwnedRepo = gamesOwnedRepo;
        this.gamesRepo = gamesRepo;
    }

    @Override
    public List<GamesOwned> getGamesOwnedByOwner(Long ownerID) {
        List<GamesOwned> gamesOwned = gamesOwnedRepo.findByOwnerID(ownerID);

        List<Long> gameIDs = new LinkedList<>();

        for (GamesOwned gameOwned : gamesOwned) {
            gameIDs.add(gameOwned.getGameID());
        }

        return gamesRepo.findAllById(gameIDs);
    }
}
