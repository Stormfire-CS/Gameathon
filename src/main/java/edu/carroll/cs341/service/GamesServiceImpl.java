package edu.carroll.cs341.service;

import edu.carroll.cs341.jpa.model.Game;
import edu.carroll.cs341.jpa.model.Login;
import edu.carroll.cs341.jpa.repo.GamesRepository;
import edu.carroll.cs341.jpa.repo.LoginRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GamesServiceImpl implements GamesService{
    private static final Logger log = LoggerFactory.getLogger(GamesServiceImpl.class);

    private final GamesRepository gamesRepo;
    private final LoginRepository loginRepo;

    public GamesServiceImpl(GamesRepository gamesRepo, LoginRepository loginRepo) {
        this.gamesRepo = gamesRepo;
        this.loginRepo = loginRepo;
    }

    @Override
    public List<Game> getAvailableGames(Long ownerID) {
        log.debug("getAvailableGames: getting games for the owner {}", ownerID);

        List<Game> games = gamesRepo.findByOwnerIDOfAdderIsNullOrOwnerIDOfAdderOrderByGameNameAsc(ownerID);

        log.debug("getAvailableGames: found {} available games", games.size());

        return games;
    }

    @Override
    public void addGame(String username, String gameName, Integer minPlayers, Integer maxPlayers) {
        log.debug("addGame: user: {} attempted to add game: {}", username, gameName);
        List<Login> users = loginRepo.findByUsernameIgnoreCase(username);

        if (users.size() != 1) {
            log.debug("addGame: found {} users for username {}", users.size(), username);
            return;
        }

        Login user = users.getFirst();

        Game game = gamesRepo.findByGameNameIgnoreCase(gameName);

        if (game != null) {
            log.debug("addGame: game {} already exists", gameName);
            return;
        }

        Game newGame = new Game(gameName);

        newGame.setMaxPlayers(maxPlayers);
        newGame.setMinPlayers(minPlayers);
        newGame.setOwnerIDOfAdder(user.getId());

        gamesRepo.save(newGame);

        log.info("addGame: user: {} added game: {}", username, gameName);
    }
}
