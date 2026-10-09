package edu.carroll.cs341.service;

import edu.carroll.cs341.jpa.model.Game;
import edu.carroll.cs341.jpa.model.Login;
import edu.carroll.cs341.jpa.repo.GamesRepository;
import edu.carroll.cs341.jpa.repo.LoginRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GamesServiceImpl implements GamesService{

    private final GamesRepository gamesRepo;
    private final LoginRepository loginRepo;

    public GamesServiceImpl(GamesRepository gamesRepo, LoginRepository loginRepo) {
        this.gamesRepo = gamesRepo;
        this.loginRepo = loginRepo;
    }

    @Override
    public List<Game> getAvailableGames(Long ownerID) {
        return gamesRepo.findByOwnerIDOfAdderIsNullOrOwnerIDOfAdderOrderByGameNameAsc(ownerID);
    }

    @Override
    public void addGame(String username, String gameName, Integer minPlayers, Integer maxPlayers) {
        List<Login> users = loginRepo.findByUsernameIgnoreCase(username);

        if (users.size() != 1) {
            return;
        }

        Login user = users.getFirst();

        Game game = gamesRepo.findByGameNameIgnoreCase(gameName);

        if (game != null) {
            return;
        }

        Game newGame = new Game(gameName);

        newGame.setMaxPlayers(maxPlayers);
        newGame.setMinPlayers(minPlayers);
        newGame.setOwnerIDOfAdder(user.getId());

        gamesRepo.save(newGame);
    }
}
