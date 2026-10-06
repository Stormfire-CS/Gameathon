package edu.carroll.cs341.service;

import edu.carroll.cs341.jpa.model.GamesOwned;
import edu.carroll.cs341.jpa.repo.GamesOwnedRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GamesOwnedServiceImpl implements GamesOwnedService {

    private final GamesOwnedRepository gamesOwnedRepo;

    public GamesOwnedServiceImpl(GamesOwnedRepository gamesOwnedRepo) {
        this.gamesOwnedRepo = gamesOwnedRepo;
    }

    @Override
    public List<GamesOwned> getGamesOwnedByOwner(Long ownerID) {

        return gamesOwnedRepo.findByOwnerID(ownerID);
    }
}
