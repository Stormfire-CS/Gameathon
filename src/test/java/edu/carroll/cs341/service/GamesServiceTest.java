package edu.carroll.cs341.service;

import edu.carroll.cs341.jpa.model.Game;
import edu.carroll.cs341.jpa.model.User;
import edu.carroll.cs341.jpa.repo.GamesRepository;
import edu.carroll.cs341.jpa.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.springframework.test.util.AssertionErrors.*;

@SpringBootTest
public class GamesServiceTest {
    private static final String username = "testuser";
    private static final String password = "testpass";
    private static final String invalidName = "used";
    private static final String invalidName2 = "used2";
    private static final String validName = "free";
    private static final Integer smaller = 1;
    private static final Integer larger = 10;
    private static final Integer negative = -1;

    @Autowired
    private GamesService gamesService;

    @Autowired
    private GamesRepository gameRepo;

    @Autowired
    private UserRepository userRepo;


    private User fakeUser = new User(username, password);
    private Game fakeGame = new Game(invalidName);
    private Game fakeGameWithUser = new Game(invalidName2);

    // Add your test users and test games here.

    @BeforeEach
    public void beforeTest() {
        assertNotNull("gamesService must be injected", gamesService);
        assertNotNull("gameRepo must be injected", gameRepo);
        assertNotNull("userRepo must be injected", userRepo);

        // Ensure dummy record is in the DB
        final List<User> users = userRepo.findByUsernameIgnoreCase(username);

        if (users.isEmpty()) {
            fakeUser = userRepo.save(fakeUser);
        } else {
            fakeUser = users.get(0);
        }
        if (!gameRepo.existsByGameName(invalidName)) {
            gameRepo.save(fakeGame);
        }
        if (fakeGameWithUser.getUserWhoAdded() == null) {
            fakeGameWithUser.setUserWhoAdded(fakeUser);
        }
        if (!gameRepo.existsByGameName(invalidName2)) {
            gameRepo.save(fakeGameWithUser);
        }
    }

    @Test
    public void addGameValidUserValidGameNameValidMinPlayersNegativeMaxPlayers () {
        assertFalse("addGameValidUserValidGameNameValidMinPlayersNegativeMaxPlayers: should be false when using negative max players", gamesService.addGame(fakeUser,validName + System.nanoTime(), smaller, negative));
    }

    @Test
    public void addGameValidUserValidGameNameNegativeMinPlayersValidMaxPlayers () {
        assertFalse("addGameValidUserValidGameNameNegativeMinPlayersValidMaxPlayers: should be false when using negative min players", gamesService.addGame(fakeUser,validName + System.nanoTime(), negative, larger));
    }

    @Test
    public void addGameValidUserValidGameNameLargerMinPlayersSmallerMaxPlayers () {
        assertFalse("addGameValidUserValidGameNameLargerMinPlayersSmallerMaxPlayers: should be false when using a min player count larger than max players", gamesService.addGame(fakeUser,validName + System.nanoTime(), larger, smaller));
    }

    @Test
    public void addGameValidUserValidGameNameValidMinPlayersNullMaxPlayers () {
        assertTrue("addGameValidUserValidGameNameValidMinPlayersNullMaxPlayers: should be true when using a null for max players", gamesService.addGame(fakeUser,validName + System.nanoTime(), smaller, null));
    }

    @Test
    public void addGameValidUserValidGameNameNegativeMinPlayersNullMaxPlayers () {
        assertFalse("addGameValidUserValidGameNameNegativeMinPlayersNullMaxPlayers: should be false when using a null for max players and negative for min players", gamesService.addGame(fakeUser,validName + System.nanoTime(), negative, null));
    }

    @Test
    public void addGameValidUserValidGameNameNullMinPlayersValidMaxPlayers () {
        assertTrue("addGameValidUserValidGameNameNullMinPlayersValidMaxPlayers: should be true when using a null for min players", gamesService.addGame(fakeUser,validName + System.nanoTime(), null, smaller));
    }

    @Test
    public void addGameValidUserValidGameNameNullMinPlayersNegativeMaxPlayers () {
        assertFalse("addGameValidUserValidGameNameNullMinPlayersNegativeMaxPlayers: should be false when using a null for min players and negative for max players", gamesService.addGame(fakeUser,validName + System.nanoTime(), null, negative));
    }

    @Test
    public void addGameValidUserValidGameNameNullMinPlayersNullMaxPlayers () {
        assertTrue("addGameValidUserValidGameNameNullMinPlayersNullMaxPlayers: should be true when using a null for min players and  max players", gamesService.addGame(fakeUser,validName + System.nanoTime(), null, null));
    }

    @Test
    public void addGameValidUserValidGameNameNegativeMinPlayersNegativeMaxPlayers () {
        assertFalse("addGameValidUserValidGameNameNegativeMinPlayersNegativeMaxPlayers: should be false when using a negative for min players and  max players", gamesService.addGame(fakeUser,validName + System.nanoTime(), negative, negative));
    }

    @Test
    public void addGameValidUserValidGameNameMinPlayersEqualsMaxPlayers () {
        assertTrue("addGameValidUserValidGameNameMinPlayersEqualsMaxPlayers: should be true when using the same number for min players and  max players", gamesService.addGame(fakeUser,validName + System.nanoTime(), smaller, smaller));
    }

    @Test
    public void addGameValidUserInvalidGameNameValidMinPlayersNegativeMaxPlayers () {
        assertFalse("addGameValidUserInvalidGameNameValidMinPlayersNegativeMaxPlayers: should be false when using negative max players and invalid name", gamesService.addGame(fakeUser,invalidName2, smaller, negative));
    }

    @Test
    public void addGameValidUserInvalidGameNameNegativeMinPlayersValidMaxPlayers () {
        assertFalse("addGameValidUserInvalidGameNameNegativeMinPlayersValidMaxPlayers: should be false when using negative min players and invalid name", gamesService.addGame(fakeUser,invalidName2, negative, larger));
    }

    @Test
    public void addGameValidUserInvalidGameNameLargerMinPlayersSmallerMaxPlayers () {
        assertFalse("addGameValidUserInvalidGameNameLargerMinPlayersSmallerMaxPlayers: should be false when using a min player count larger than max players and invalid name", gamesService.addGame(fakeUser,invalidName2, larger, smaller));
    }

    @Test
    public void addGameValidUserInvalidGameNameValidMinPlayersNullMaxPlayers () {
        assertFalse("addGameValidUserInvalidGameNameValidMinPlayersNullMaxPlayers: should be false when using invalid name with valid min players and null max players", gamesService.addGame(fakeUser,invalidName2, smaller, null));
    }

    @Test
    public void addGameValidUserInvalidGameNameNegativeMinPlayersNullMaxPlayers () {
        assertFalse("addGameValidUserInvalidGameNameNegativeMinPlayersNullMaxPlayers: should be false when using a null for max players and negative for min players and using an invalid name", gamesService.addGame(fakeUser,invalidName2, negative, null));
    }

    @Test
    public void addGameValidUserInvalidGameNameNullMinPlayersValidMaxPlayers () {
        assertFalse("addGameValidUserInvalidGameNameNullMinPlayersValidMaxPlayers: should be false an invalid name with null min players and valid max players", gamesService.addGame(fakeUser,invalidName2, null, smaller));
    }

    @Test
    public void addGameValidUserInvalidGameNameNullMinPlayersNegativeMaxPlayers () {
        assertFalse("addGameValidUserInvalidGameNameNullMinPlayersNegativeMaxPlayers: should be false when using a null for min players and negative for max players and using an invalid name", gamesService.addGame(fakeUser,invalidName2, null, negative));
    }

    @Test
    public void addGameValidUserInvalidGameNameNullMinPlayersNullMaxPlayers () {
        assertFalse("addGameValidUserInvalidGameNameNullMinPlayersNullMaxPlayers: should be false when using a null for min players and max players and using an invalid name", gamesService.addGame(fakeUser,invalidName2, null, null));
    }

    @Test
    public void addGameValidUserInvalidGameNameNegativeMinPlayersNegativeMaxPlayers () {
        assertFalse("addGameValidUserInvalidGameNameNegativeMinPlayersNegativeMaxPlayers: should be false when using a negative for min players and  max players and using an invalid name", gamesService.addGame(fakeUser,invalidName2, negative, negative));
    }

    @Test
    public void addGameValidUserInvalidGameNameMinPlayersEqualsMaxPlayers () {
        assertFalse("addGameValidUserInvalidGameNameMinPlayersEqualsMaxPlayers: should be false when using the same number for min players and  max players and using an invalid name", gamesService.addGame(fakeUser,invalidName2, smaller, smaller));
    }

    @Test
    public void addGameValidUserNullGameNameValidMinPlayersNegativeMaxPlayers () {
        assertFalse("addGameValidUserNullGameNameValidMinPlayersNegativeMaxPlayers: should be false when using negative max players and a null name", gamesService.addGame(fakeUser,null, smaller, negative));
    }

    @Test
    public void addGameValidUserNullGameNameNegativeMinPlayersValidMaxPlayers () {
        assertFalse("addGameValidUserNullGameNameNegativeMinPlayersValidMaxPlayers: should be false when using negative min players and a null name", gamesService.addGame(fakeUser,null, negative, larger));
    }

    @Test
    public void addGameValidUserNullGameNameLargerMinPlayersSmallerMaxPlayers () {
        assertFalse("addGameValidUserNullGameNameLargerMinPlayersSmallerMaxPlayers: should be false when using a min player count larger than max players and a null name", gamesService.addGame(fakeUser,null, larger, smaller));
    }

    @Test
    public void addGameValidUserNullGameNameValidMinPlayersNullMaxPlayers () {
        assertFalse("addGameValidUserNullGameNameValidMinPlayersNullMaxPlayers: should be false when using null name with valid min players and null max players", gamesService.addGame(fakeUser,null, smaller, null));
    }

    @Test
    public void addGameValidUserNullGameNameNegativeMinPlayersNullMaxPlayers () {
        assertFalse("addGameValidUserNullGameNameNegativeMinPlayersNullMaxPlayers: should be false when using a null for max players and negative for min players and using a null name", gamesService.addGame(fakeUser,null, negative, null));
    }

    @Test
    public void addGameValidUserNullGameNameNullMinPlayersValidMaxPlayers () {
        assertFalse("addGameValidUserInvalidGameNameNullMinPlayersValidMaxPlayers: should be false a null name with null min players and valid max players", gamesService.addGame(fakeUser,null, null, smaller));
    }

    @Test
    public void addGameValidUserNullGameNameNullMinPlayersNegativeMaxPlayers () {
        assertFalse("addGameValidUserNullGameNameNullMinPlayersNegativeMaxPlayers: should be false when using a null for min players and negative for max players and using a null name", gamesService.addGame(fakeUser,null, null, negative));
    }

    @Test
    public void addGameValidUserNullGameNameNullMinPlayersNullMaxPlayers () {
        assertFalse("addGameValidUserNullGameNameNullMinPlayersNullMaxPlayers: should be false when using a null for min players and max players and using a null name", gamesService.addGame(fakeUser,null, null, null));
    }

    @Test
    public void addGameValidUserNullGameNameNegativeMinPlayersNegativeMaxPlayers () {
        assertFalse("addGameValidUserNullGameNameNegativeMinPlayersNegativeMaxPlayers: should be false when using a negative for min players and  max players and using a null name", gamesService.addGame(fakeUser,null, negative, negative));
    }

    @Test
    public void addGameValidUserNullGameNameMinPlayersEqualsMaxPlayers () {
        assertFalse("addGameValidUserNullGameNameMinPlayersEqualsMaxPlayers: should be false when using the same number for min players and  max players and using a null name", gamesService.addGame(fakeUser,null, smaller, smaller));
    }

    @Test
    public void addGameNullUserValidGameNameValidMinPlayersNegativeMaxPlayers () {
        assertFalse("addGameNullUserValidGameNameValidMinPlayersNegativeMaxPlayers: should be false when using negative max players", gamesService.addGame(null,validName + System.nanoTime(), smaller, negative));
    }

    @Test
    public void addGameNullUserValidGameNameNegativeMinPlayersValidMaxPlayers () {
        assertFalse("addGameNullUserValidGameNameNegativeMinPlayersValidMaxPlayers: should be false when using negative min players", gamesService.addGame(null,validName + System.nanoTime(), negative, larger));
    }

    @Test
    public void addGameNullUserValidGameNameLargerMinPlayersSmallerMaxPlayers () {
        assertFalse("addGameNullUserValidGameNameLargerMinPlayersSmallerMaxPlayers: should be false when using a min player count larger than max players", gamesService.addGame(null,validName + System.nanoTime(), larger, smaller));
    }

    @Test
    public void addGameNullUserValidGameNameValidMinPlayersNullMaxPlayers () {
        assertFalse("addGameNullUserValidGameNameValidMinPlayersNullMaxPlayers: should be when using a null user", gamesService.addGame(null,validName + System.nanoTime(), smaller, null));
    }

    @Test
    public void addGameNullUserValidGameNameNegativeMinPlayersNullMaxPlayers () {
        assertFalse("addGameNullUserValidGameNameNegativeMinPlayersNullMaxPlayers: should be false when using a null for max players and negative for min players", gamesService.addGame(null,validName + System.nanoTime(), negative, null));
    }

    @Test
    public void addGameNullUserValidGameNameNullMinPlayersValidMaxPlayers () {
        assertFalse("addGameNullUserValidGameNameNullMinPlayersValidMaxPlayers: should be false when using a null user", gamesService.addGame(null,validName + System.nanoTime(), null, smaller));
    }

    @Test
    public void addGameNullUserValidGameNameNullMinPlayersNegativeMaxPlayers () {
        assertFalse("addGameNullUserValidGameNameNullMinPlayersNegativeMaxPlayers: should be false when using a null for min players and negative for max players", gamesService.addGame(null,validName + System.nanoTime(), null, negative));
    }

    @Test
    public void addGameNullUserValidGameNameNullMinPlayersNullMaxPlayers () {
        assertFalse("addGameNullUserValidGameNameNullMinPlayersNullMaxPlayers: should be false when using a null user", gamesService.addGame(null,validName + System.nanoTime(), null, null));
    }

    @Test
    public void addGameNullUserValidGameNameNegativeMinPlayersNegativeMaxPlayers () {
        assertFalse("addGameNullUserValidGameNameNegativeMinPlayersNegativeMaxPlayers: should be false when using a negative for min players and  max players", gamesService.addGame(null,validName + System.nanoTime(), negative, negative));
    }

    @Test
    public void addGameNullUserValidGameNameMinPlayersEqualsMaxPlayers () {
        assertFalse("addGameNullUserValidGameNameMinPlayersEqualsMaxPlayers: should be false when using a null user", gamesService.addGame(null,validName + System.nanoTime(), smaller, smaller));
    }

    @Test
    public void addGameNullUserInvalidGameNameValidMinPlayersNegativeMaxPlayers () {
        assertFalse("addGameNullUserInvalidGameNameValidMinPlayersNegativeMaxPlayers: should be false when using negative max players and invalid name", gamesService.addGame(null,invalidName, smaller, negative));
    }

    @Test
    public void addGameNullUserInvalidGameNameNegativeMinPlayersValidMaxPlayers () {
        assertFalse("addGameNullUserInvalidGameNameNegativeMinPlayersValidMaxPlayers: should be false when using negative min players and invalid name", gamesService.addGame(null,invalidName, negative, larger));
    }

    @Test
    public void addGameNullUserInvalidGameNameLargerMinPlayersSmallerMaxPlayers () {
        assertFalse("addGameNullUserInvalidGameNameLargerMinPlayersSmallerMaxPlayers: should be false when using a min player count larger than max players and invalid name", gamesService.addGame(null,invalidName, larger, smaller));
    }

    @Test
    public void addGameNullUserInvalidGameNameValidMinPlayersNullMaxPlayers () {
        assertFalse("addGameNullUserInvalidGameNameValidMinPlayersNullMaxPlayers: should be false when using invalid name with valid min players and null max players", gamesService.addGame(null,invalidName, smaller, null));
    }

    @Test
    public void addGameNullUserInvalidGameNameNegativeMinPlayersNullMaxPlayers () {
        assertFalse("addGameNullUserInvalidGameNameNegativeMinPlayersNullMaxPlayers: should be false when using a null for max players and negative for min players and using an invalid name", gamesService.addGame(null,invalidName, negative, null));
    }

    @Test
    public void addGameNullUserInvalidGameNameNullMinPlayersValidMaxPlayers () {
        assertFalse("addGameNullUserInvalidGameNameNullMinPlayersValidMaxPlayers: should be false an invalid name with null min players and valid max players", gamesService.addGame(null,invalidName, null, smaller));
    }

    @Test
    public void addGameNullUserInvalidGameNameNullMinPlayersNegativeMaxPlayers () {
        assertFalse("addGameNullUserInvalidGameNameNullMinPlayersNegativeMaxPlayers: should be false when using a null for min players and negative for max players and using an invalid name", gamesService.addGame(null,invalidName, null, negative));
    }

    @Test
    public void addGameNullUserInvalidGameNameNullMinPlayersNullMaxPlayers () {
        assertFalse("addGameNullUserInvalidGameNameNullMinPlayersNullMaxPlayers: should be false when using a null for min players and max players and using an invalid name", gamesService.addGame(null,invalidName, null, null));
    }

    @Test
    public void addGameNullUserInvalidGameNameNegativeMinPlayersNegativeMaxPlayers () {
        assertFalse("addGameNullUserInvalidGameNameNegativeMinPlayersNegativeMaxPlayers: should be false when using a negative for min players and  max players and using an invalid name", gamesService.addGame(null,invalidName, negative, negative));
    }

    @Test
    public void addGameNullUserInvalidGameNameMinPlayersEqualsMaxPlayers () {
        assertFalse("addGameNullUserInvalidGameNameMinPlayersEqualsMaxPlayers: should be false when using the same number for min players and  max players and using an invalid name", gamesService.addGame(null,invalidName, smaller, smaller));
    }

    @Test
    public void addGameNullUserNullGameNameValidMinPlayersNegativeMaxPlayers () {
        assertFalse("addGameNullUserNullGameNameValidMinPlayersNegativeMaxPlayers: should be false when using negative max players and a null name", gamesService.addGame(null,null, smaller, negative));
    }

    @Test
    public void addGameNullUserNullGameNameNegativeMinPlayersValidMaxPlayers () {
        assertFalse("addGameNullUserNullGameNameNegativeMinPlayersValidMaxPlayers: should be false when using negative min players and a null name", gamesService.addGame(null,null, negative, larger));
    }

    @Test
    public void addGameNullUserNullGameNameLargerMinPlayersSmallerMaxPlayers () {
        assertFalse("addGameNullUserNullGameNameLargerMinPlayersSmallerMaxPlayers: should be false when using a min player count larger than max players and a null name", gamesService.addGame(null,null, larger, smaller));
    }

    @Test
    public void addGameNullUserNullGameNameValidMinPlayersNullMaxPlayers () {
        assertFalse("addGameNullUserNullGameNameValidMinPlayersNullMaxPlayers: should be false when using null name with valid min players and null max players", gamesService.addGame(null,null, smaller, null));
    }

    @Test
    public void addGameNullUserNullGameNameNegativeMinPlayersNullMaxPlayers () {
        assertFalse("addGameNullUserNullGameNameNegativeMinPlayersNullMaxPlayers: should be false when using a null for max players and negative for min players and using a null name", gamesService.addGame(null,null, negative, null));
    }

    @Test
    public void addGameNullUserNullGameNameNullMinPlayersValidMaxPlayers () {
        assertFalse("addGameNullUserNullGameNameNullMinPlayersValidMaxPlayers: should be false a null name with null min players and valid max players", gamesService.addGame(null,null, null, smaller));
    }

    @Test
    public void addGameNullUserNullGameNameNullMinPlayersNegativeMaxPlayers () {
        assertFalse("addGameNullUserNullGameNameNullMinPlayersNegativeMaxPlayers: should be false when using a null for min players and negative for max players and using a null name", gamesService.addGame(null,null, null, negative));
    }

    @Test
    public void addGameNullUserNullGameNameNullMinPlayersNullMaxPlayers () {
        assertFalse("addGameNullUserNullGameNameNullMinPlayersNullMaxPlayers: should be false when using a null for min players and max players and using a null name", gamesService.addGame(null,null, null, null));
    }

    @Test
    public void addGameNullUserNullGameNameNegativeMinPlayersNegativeMaxPlayers () {
        assertFalse("addGameNullUserNullGameNameNegativeMinPlayersNegativeMaxPlayers: should be false when using a negative for min players and  max players and using a null name", gamesService.addGame(null,null, negative, negative));
    }

    @Test
    public void addGameNullUserNullGameNameMinPlayersEqualsMaxPlayers () {
        assertFalse("addGameNullUserNullGameNameMinPlayersEqualsMaxPlayers: should be false when using the same number for min players and  max players and using a null name", gamesService.addGame(null,null, smaller, smaller));
    }

}