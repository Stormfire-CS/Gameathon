package edu.carroll.cs341.service;

import edu.carroll.cs341.jpa.model.Games;
import edu.carroll.cs341.jpa.model.GamesOwned;
import edu.carroll.cs341.jpa.model.Login;
import edu.carroll.cs341.jpa.repo.GamesOwnedRepository;
import edu.carroll.cs341.jpa.repo.GamesRepository;
import edu.carroll.cs341.jpa.repo.LoginRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
public class GamesOwnedServiceImpl implements GamesOwnedService {

    private final GamesOwnedRepository gamesOwnedRepo;
    private final LoginRepository loginRepo;
    private final GamesRepository gamesRepo;


    public GamesOwnedServiceImpl(GamesOwnedRepository gamesOwnedRepo, LoginRepository loginRepo, GamesRepository gamesRepo) {
        this.gamesOwnedRepo = gamesOwnedRepo;
        this.loginRepo = loginRepo;
        this.gamesRepo = gamesRepo;
    }

    @Override
    public void addGame(String username, Long gameID, Integer yearProduced) {

        List<Login> users = loginRepo.findByUsernameIgnoreCase(username);

        if (users.size() != 1) {
            return;
        }

        Login user = users.getFirst();

        Games game = gamesRepo.findByGameID(gameID);

        if (game == null) {
            return; //Later I want to safely allow the user to generate new game entries in our table.
        }

        GamesOwned gamesOwned = new GamesOwned(game, user.getId());

        gamesOwned.setYearProduced(yearProduced);

        gamesOwnedRepo.save(gamesOwned);
    }

    @Override
    public List<GamesOwned> getGamesOwnedByUsername(String username) {
        List<Login> users = loginRepo.findByUsernameIgnoreCase(username);

        if (users.size() != 1) {
            return new LinkedList<>();
        }

        Long ownerID = users.getFirst().getId();

        return gamesOwnedRepo.findByOwnerID(ownerID);
    }
}
