package edu.carroll.cs341.service;

import edu.carroll.cs341.jpa.model.Games;
import edu.carroll.cs341.jpa.repo.GamesRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GamesServiceImpl implements GamesService{

    private final GamesRepository gamesRepo;

    public GamesServiceImpl(GamesRepository gamesRepo) {
        this.gamesRepo = gamesRepo;
    }

    @Override
    public List<Games> getAllGames() {
        return gamesRepo.findAll();
    }
}
