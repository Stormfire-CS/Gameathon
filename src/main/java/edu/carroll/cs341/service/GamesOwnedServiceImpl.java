package edu.carroll.cs341.service;

import edu.carroll.cs341.jpa.model.GamesOwned;
import edu.carroll.cs341.jpa.model.Login;
import edu.carroll.cs341.jpa.repo.GamesOwnedRepository;
import edu.carroll.cs341.jpa.repo.LoginRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
public class GamesOwnedServiceImpl implements GamesOwnedService {

    private final GamesOwnedRepository gamesOwnedRepo;
    private final LoginRepository loginRepo;

    public GamesOwnedServiceImpl(GamesOwnedRepository gamesOwnedRepo, LoginRepository loginRepo) {
        this.gamesOwnedRepo = gamesOwnedRepo;
        this.loginRepo = loginRepo;
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
