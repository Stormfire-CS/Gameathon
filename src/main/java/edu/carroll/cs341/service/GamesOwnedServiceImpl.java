package edu.carroll.cs341.service;

import edu.carroll.cs341.jpa.model.Game;
import edu.carroll.cs341.jpa.model.GameOwned;
import edu.carroll.cs341.jpa.model.Login;
import edu.carroll.cs341.jpa.repo.GamesOwnedRepository;
import edu.carroll.cs341.jpa.repo.GamesRepository;
import edu.carroll.cs341.jpa.repo.LoginRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
public class GamesOwnedServiceImpl implements GamesOwnedService {
    private static final Logger log = LoggerFactory.getLogger(GamesOwnedServiceImpl.class);

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
        log.debug("addGame: user {} attempted to add game {}", username, gameID);

        List<Login> users = loginRepo.findByUsernameIgnoreCase(username);

        if (users.size() != 1) {
            log.debug("addGame: found {} users for username {}", users.size(), username);
            return;
        }

        Login user = users.getFirst();

        Game game = gamesRepo.findByGameID(gameID);

        if (game == null) {
            log.debug("addGame: game {} was not found", gameID);
            return;
        }

        GameOwned gameOwned = new GameOwned(game, user.getId());

        gameOwned.setYearProduced(yearProduced);

        gamesOwnedRepo.save(gameOwned);

        log.info("addGame: user {} added game {}", username, gameID);
    }

    @Override
    public List<GameOwned> getGamesOwnedByUsername(String username) {
        log.debug("getGamesOwnedByUsername: getting games for {}", username);

        List<Login> users = loginRepo.findByUsernameIgnoreCase(username);

        if (users.size() != 1) {
            log.debug("getGamesOwnedByUsername: found {} users for username '{}'", users.size(), username);
            return new LinkedList<>();
        }

        Long ownerID = users.getFirst().getId();
        List<GameOwned> games = gamesOwnedRepo.findByOwnerID(ownerID);

        log.info("getGamesOwnedByUsername: found {} owned games for '{}'", games.size(), username);
        return games;
    }
}
