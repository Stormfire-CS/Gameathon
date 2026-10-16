package edu.carroll.cs341.service;

import edu.carroll.cs341.jpa.model.Game;
import edu.carroll.cs341.jpa.model.User;
import edu.carroll.cs341.jpa.repo.GameRepository;
import edu.carroll.cs341.jpa.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.springframework.test.util.AssertionErrors.*;

@SpringBootTest
public class GamesServiceTest {

    @Autowired
    private GamesService gamesService;

    @Autowired
    private GameRepository gameRepo;

    @Autowired
    private UserRepository userRepo;

    // Add your test users and test games here.

    @BeforeEach
    public void beforeTest() {
        assertNotNull("gamesService must be injected", gamesService);
        assertNotNull("gameRepo must be injected", gameRepo);
        assertNotNull("userRepo must be injected", userRepo);
    }
}
