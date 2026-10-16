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

/**
 * Tests the functionality of the {@link GamesService} implementation.
 *
 * <p>This test class verifies that games can be added only when the supplied
 * user, game name, and player counts satisfy the service's validation rules.
 * It tests valid and invalid game names, null users, null game names, negative
 * player counts, and minimum and maximum player count combinations.</p>
 *
 * <p>The Spring application context is loaded to provide the service and
 * repository dependencies used by these integration tests.</p>
 *
 * <p><b>Sources Used:</b></p>
 * <ul>
 *     <li>ChatGPT assisted with Javadoc documentation for this file.
 *     All comments were reviewed by the author. No code was generated.</li>
 * </ul>
 *
 * @author Max Montowski
 */
@SpringBootTest
public class GamesServiceTest {

    /** The username used for the test user. */
    private static final String username = "testuser";

    /** The raw password used to create the test user. */
    private static final String password = "testpass";

    /** The name of a game used to test an existing public game. */
    private static final String invalidName = "used";

    /** The name of a game used to test an existing user-owned game. */
    private static final String invalidName2 = "used2";

    /** The base name used to create unique game names for valid tests. */
    private static final String validName = "free";

    /** A smaller valid player count used in test cases. */
    private static final Integer smaller = 1;

    /** A larger valid player count used in test cases. */
    private static final Integer larger = 10;

    /** A negative player count used to test invalid input. */
    private static final Integer negative = -1;

    /** The game service being tested, injected by Spring. */
    @Autowired
    private GamesService gamesService;

    /** The repository used to access game records during testing. */
    @Autowired
    private GamesRepository gameRepo;

    /** The repository used to access user records during testing. */
    @Autowired
    private UserRepository userRepo;

    /** The test user associated with user-owned game records. */
    private User fakeUser = new User(username, password);

    /** A public game used to test duplicate game names. */
    private Game fakeGame = new Game(invalidName);

    /** A user-owned game used to test duplicate game names. */
    private Game fakeGameWithUser = new Game(invalidName2);

    /**
     * Ensures that the required repositories, service, test user, and test
     * games are available before each test executes.
     *
     * <p>Existing records are reused when they are already present in the
     * database to avoid unnecessarily creating duplicate test records.</p>
     */
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

    /**
     * Verifies that a game cannot be added when the maximum player count
     * is negative.
     */
    @Test
    public void addGameValidUserValidGameNameValidMinPlayersNegativeMaxPlayers () {
        assertFalse("addGameValidUserValidGameNameValidMinPlayersNegativeMaxPlayers: should be false when using negative max players", gamesService.addGame(fakeUser,validName + System.nanoTime(), smaller, negative));
    }

    /**
     * Verifies that a game cannot be added when the minimum player count
     * is negative.
     */
    @Test
    public void addGameValidUserValidGameNameNegativeMinPlayersValidMaxPlayers () {
        assertFalse("addGameValidUserValidGameNameNegativeMinPlayersValidMaxPlayers: should be false when using negative min players", gamesService.addGame(fakeUser,validName + System.nanoTime(), negative, larger));
    }

    /**
     * Verifies that a game cannot be added when the minimum player count
     * exceeds the maximum player count.
     */
    @Test
    public void addGameValidUserValidGameNameLargerMinPlayersSmallerMaxPlayers () {
        assertFalse("addGameValidUserValidGameNameLargerMinPlayersSmallerMaxPlayers: should be false when using a min player count larger than max players", gamesService.addGame(fakeUser,validName + System.nanoTime(), larger, smaller));
    }

    /**
     * Verifies that a game can be added when the maximum player count is null.
     */
    @Test
    public void addGameValidUserValidGameNameValidMinPlayersNullMaxPlayers () {
        assertTrue("addGameValidUserValidGameNameValidMinPlayersNullMaxPlayers: should be true when using a null for max players", gamesService.addGame(fakeUser,validName + System.nanoTime(), smaller, null));
    }

    /**
     * Verifies that a game cannot be added when the minimum player count
     * is negative and the maximum player count is null.
     */
    @Test
    public void addGameValidUserValidGameNameNegativeMinPlayersNullMaxPlayers () {
        assertFalse("addGameValidUserValidGameNameNegativeMinPlayersNullMaxPlayers: should be false when using a null for max players and negative for min players", gamesService.addGame(fakeUser,validName + System.nanoTime(), negative, null));
    }

    /**
     * Verifies that a game can be added when the minimum player count is null.
     */
    @Test
    public void addGameValidUserValidGameNameNullMinPlayersValidMaxPlayers () {
        assertTrue("addGameValidUserValidGameNameNullMinPlayersValidMaxPlayers: should be true when using a null for min players", gamesService.addGame(fakeUser,validName + System.nanoTime(), null, smaller));
    }

    /**
     * Verifies that a game cannot be added when the maximum player count
     * is negative and the minimum player count is null.
     */
    @Test
    public void addGameValidUserValidGameNameNullMinPlayersNegativeMaxPlayers () {
        assertFalse("addGameValidUserValidGameNameNullMinPlayersNegativeMaxPlayers: should be false when using a null for min players and negative for max players", gamesService.addGame(fakeUser,validName + System.nanoTime(), null, negative));
    }

    /**
     * Verifies that a game can be added when both player counts are null.
     */
    @Test
    public void addGameValidUserValidGameNameNullMinPlayersNullMaxPlayers () {
        assertTrue("addGameValidUserValidGameNameNullMinPlayersNullMaxPlayers: should be true when using a null for min players and  max players", gamesService.addGame(fakeUser,validName + System.nanoTime(), null, null));
    }

    /**
     * Verifies that a game cannot be added when both player counts are negative.
     */
    @Test
    public void addGameValidUserValidGameNameNegativeMinPlayersNegativeMaxPlayers () {
        assertFalse("addGameValidUserValidGameNameNegativeMinPlayersNegativeMaxPlayers: should be false when using a negative for min players and  max players", gamesService.addGame(fakeUser,validName + System.nanoTime(), negative, negative));
    }

    /**
     * Verifies that a game can be added when the minimum and maximum
     * player counts are equal.
     */
    @Test
    public void addGameValidUserValidGameNameMinPlayersEqualsMaxPlayers () {
        assertTrue("addGameValidUserValidGameNameMinPlayersEqualsMaxPlayers: should be true when using the same number for min players and  max players", gamesService.addGame(fakeUser,validName + System.nanoTime(), smaller, smaller));
    }

    /**
     * Verifies that adding a game with an existing user-owned game name
     * fails when the maximum player count is negative.
     */
    @Test
    public void addGameValidUserInvalidGameNameValidMinPlayersNegativeMaxPlayers () {
        assertFalse("addGameValidUserInvalidGameNameValidMinPlayersNegativeMaxPlayers: should be false when using negative max players and invalid name", gamesService.addGame(fakeUser,invalidName2, smaller, negative));
    }

    /**
     * Verifies that adding a game with an existing user-owned game name
     * fails when the minimum player count is negative.
     */
    @Test
    public void addGameValidUserInvalidGameNameNegativeMinPlayersValidMaxPlayers () {
        assertFalse("addGameValidUserInvalidGameNameNegativeMinPlayersValidMaxPlayers: should be false when using negative min players and invalid name", gamesService.addGame(fakeUser,invalidName2, negative, larger));
    }

    /**
     * Verifies that adding a game with an existing user-owned game name
     * fails when the minimum player count exceeds the maximum.
     */
    @Test
    public void addGameValidUserInvalidGameNameLargerMinPlayersSmallerMaxPlayers () {
        assertFalse("addGameValidUserInvalidGameNameLargerMinPlayersSmallerMaxPlayers: should be false when using a min player count larger than max players and invalid name", gamesService.addGame(fakeUser,invalidName2, larger, smaller));
    }

    /**
     * Verifies that adding a game with an existing user-owned game name
     * fails when the maximum player count is null.
     */
    @Test
    public void addGameValidUserInvalidGameNameValidMinPlayersNullMaxPlayers () {
        assertFalse("addGameValidUserInvalidGameNameValidMinPlayersNullMaxPlayers: should be false when using invalid name with valid min players and null max players", gamesService.addGame(fakeUser,invalidName2, smaller, null));
    }

    /**
     * Verifies that adding a game with an existing user-owned game name
     * fails when the minimum player count is negative and the maximum is null.
     */
    @Test
    public void addGameValidUserInvalidGameNameNegativeMinPlayersNullMaxPlayers () {
        assertFalse("addGameValidUserInvalidGameNameNegativeMinPlayersNullMaxPlayers: should be false when using a null for max players and negative for min players and using an invalid name", gamesService.addGame(fakeUser,invalidName2, negative, null));
    }

    /**
     * Verifies that adding a game with an existing user-owned game name
     * fails when the minimum player count is null.
     */
    @Test
    public void addGameValidUserInvalidGameNameNullMinPlayersValidMaxPlayers () {
        assertFalse("addGameValidUserInvalidGameNameNullMinPlayersValidMaxPlayers: should be false an invalid name with null min players and valid max players", gamesService.addGame(fakeUser,invalidName2, null, smaller));
    }

    /**
     * Verifies that adding a game with an existing user-owned game name
     * fails when the maximum player count is negative and the minimum is null.
     */
    @Test
    public void addGameValidUserInvalidGameNameNullMinPlayersNegativeMaxPlayers () {
        assertFalse("addGameValidUserInvalidGameNameNullMinPlayersNegativeMaxPlayers: should be false when using a null for min players and negative for max players and using an invalid name", gamesService.addGame(fakeUser,invalidName2, null, negative));
    }

    /**
     * Verifies that adding a game with an existing user-owned game name
     * fails when both player counts are null.
     */
    @Test
    public void addGameValidUserInvalidGameNameNullMinPlayersNullMaxPlayers () {
        assertFalse("addGameValidUserInvalidGameNameNullMinPlayersNullMaxPlayers: should be false when using a null for min players and max players and using an invalid name", gamesService.addGame(fakeUser,invalidName2, null, null));
    }

    /**
     * Verifies that adding a game with an existing user-owned game name
     * fails when both player counts are negative.
     */
    @Test
    public void addGameValidUserInvalidGameNameNegativeMinPlayersNegativeMaxPlayers () {
        assertFalse("addGameValidUserInvalidGameNameNegativeMinPlayersNegativeMaxPlayers: should be false when using a negative for min players and  max players and using an invalid name", gamesService.addGame(fakeUser,invalidName2, negative, negative));
    }

    /**
     * Verifies that adding a game with an existing user-owned game name
     * fails when the minimum and maximum player counts are equal.
     */
    @Test
    public void addGameValidUserInvalidGameNameMinPlayersEqualsMaxPlayers () {
        assertFalse("addGameValidUserInvalidGameNameMinPlayersEqualsMaxPlayers: should be false when using the same number for min players and  max players and using an invalid name", gamesService.addGame(fakeUser,invalidName2, smaller, smaller));
    }

    /**
     * Verifies that adding a game with a null name fails when the maximum
     * player count is negative.
     */
    @Test
    public void addGameValidUserNullGameNameValidMinPlayersNegativeMaxPlayers () {
        assertFalse("addGameValidUserNullGameNameValidMinPlayersNegativeMaxPlayers: should be false when using negative max players and a null name", gamesService.addGame(fakeUser,null, smaller, negative));
    }

    /**
     * Verifies that adding a game with a null name fails when the minimum
     * player count is negative.
     */
    @Test
    public void addGameValidUserNullGameNameNegativeMinPlayersValidMaxPlayers () {
        assertFalse("addGameValidUserNullGameNameNegativeMinPlayersValidMaxPlayers: should be false when using negative min players and a null name", gamesService.addGame(fakeUser,null, negative, larger));
    }

    /**
     * Verifies that adding a game with a null name fails when the minimum
     * player count exceeds the maximum.
     */
    @Test
    public void addGameValidUserNullGameNameLargerMinPlayersSmallerMaxPlayers () {
        assertFalse("addGameValidUserNullGameNameLargerMinPlayersSmallerMaxPlayers: should be false when using a min player count larger than max players and a null name", gamesService.addGame(fakeUser,null, larger, smaller));
    }

    /**
     * Verifies that adding a game with a null name fails when the maximum
     * player count is null.
     */
    @Test
    public void addGameValidUserNullGameNameValidMinPlayersNullMaxPlayers () {
        assertFalse("addGameValidUserNullGameNameValidMinPlayersNullMaxPlayers: should be false when using null name with valid min players and null max players", gamesService.addGame(fakeUser,null, smaller, null));
    }

    /**
     * Verifies that adding a game with a null name fails when the minimum
     * player count is negative and the maximum is null.
     */
    @Test
    public void addGameValidUserNullGameNameNegativeMinPlayersNullMaxPlayers () {
        assertFalse("addGameValidUserNullGameNameNegativeMinPlayersNullMaxPlayers: should be false when using a null for max players and negative for min players and using a null name", gamesService.addGame(fakeUser,null, negative, null));
    }

    /**
     * Verifies that adding a game with a null name fails when the minimum
     * player count is null and the maximum is valid.
     */
    @Test
    public void addGameValidUserNullGameNameNullMinPlayersValidMaxPlayers () {
        assertFalse("addGameValidUserInvalidGameNameNullMinPlayersValidMaxPlayers: should be false a null name with null min players and valid max players", gamesService.addGame(fakeUser,null, null, smaller));
    }

    /**
     * Verifies that adding a game with a null name fails when the minimum
     * player count is null and the maximum is negative.
     */
    @Test
    public void addGameValidUserNullGameNameNullMinPlayersNegativeMaxPlayers () {
        assertFalse("addGameValidUserNullGameNameNullMinPlayersNegativeMaxPlayers: should be false when using a null for min players and negative for max players and using a null name", gamesService.addGame(fakeUser,null, null, negative));
    }

    /**
     * Verifies that adding a game with a null name fails when both player
     * counts are null.
     */
    @Test
    public void addGameValidUserNullGameNameNullMinPlayersNullMaxPlayers () {
        assertFalse("addGameValidUserNullGameNameNullMinPlayersNullMaxPlayers: should be false when using a null for min players and max players and using a null name", gamesService.addGame(fakeUser,null, null, null));
    }

    /**
     * Verifies that adding a game with a null name fails when both player
     * counts are negative.
     */
    @Test
    public void addGameValidUserNullGameNameNegativeMinPlayersNegativeMaxPlayers () {
        assertFalse("addGameValidUserNullGameNameNegativeMinPlayersNegativeMaxPlayers: should be false when using a negative for min players and  max players and using a null name", gamesService.addGame(fakeUser,null, negative, negative));
    }

    /**
     * Verifies that adding a game with a null name fails when the minimum
     * and maximum player counts are equal.
     */
    @Test
    public void addGameValidUserNullGameNameMinPlayersEqualsMaxPlayers () {
        assertFalse("addGameValidUserNullGameNameMinPlayersEqualsMaxPlayers: should be false when using the same number for min players and  max players and using a null name", gamesService.addGame(fakeUser,null, smaller, smaller));
    }

    /**
     * Verifies that adding a game fails when the user is null and the
     * maximum player count is negative.
     */
    @Test
    public void addGameNullUserValidGameNameValidMinPlayersNegativeMaxPlayers () {
        assertFalse("addGameNullUserValidGameNameValidMinPlayersNegativeMaxPlayers: should be false when using negative max players", gamesService.addGame(null,validName + System.nanoTime(), smaller, negative));
    }

    /**
     * Verifies that adding a game fails when the user is null and the
     * minimum player count is negative.
     */
    @Test
    public void addGameNullUserValidGameNameNegativeMinPlayersValidMaxPlayers () {
        assertFalse("addGameNullUserValidGameNameNegativeMinPlayersValidMaxPlayers: should be false when using negative min players", gamesService.addGame(null,validName + System.nanoTime(), negative, larger));
    }

    /**
     * Verifies that adding a game fails when the user is null and the
     * minimum player count exceeds the maximum.
     */
    @Test
    public void addGameNullUserValidGameNameLargerMinPlayersSmallerMaxPlayers () {
        assertFalse("addGameNullUserValidGameNameLargerMinPlayersSmallerMaxPlayers: should be false when using a min player count larger than max players", gamesService.addGame(null,validName + System.nanoTime(), larger, smaller));
    }

    /**
     * Verifies that adding a game fails when the user is null and the
     * maximum player count is null.
     */
    @Test
    public void addGameNullUserValidGameNameValidMinPlayersNullMaxPlayers () {
        assertFalse("addGameNullUserValidGameNameValidMinPlayersNullMaxPlayers: should be when using a null user", gamesService.addGame(null,validName + System.nanoTime(), smaller, null));
    }

    /**
     * Verifies that adding a game fails when the user is null, the minimum
     * player count is negative, and the maximum is null.
     */
    @Test
    public void addGameNullUserValidGameNameNegativeMinPlayersNullMaxPlayers () {
        assertFalse("addGameNullUserValidGameNameNegativeMinPlayersNullMaxPlayers: should be false when using a null for max players and negative for min players", gamesService.addGame(null,validName + System.nanoTime(), negative, null));
    }

    /**
     * Verifies that adding a game fails when the user is null and the
     * minimum player count is null.
     */
    @Test
    public void addGameNullUserValidGameNameNullMinPlayersValidMaxPlayers () {
        assertFalse("addGameNullUserValidGameNameNullMinPlayersValidMaxPlayers: should be false when using a null user", gamesService.addGame(null,validName + System.nanoTime(), null, smaller));
    }

    /**
     * Verifies that adding a game fails when the user is null, the minimum
     * player count is null, and the maximum is negative.
     */
    @Test
    public void addGameNullUserValidGameNameNullMinPlayersNegativeMaxPlayers () {
        assertFalse("addGameNullUserValidGameNameNullMinPlayersNegativeMaxPlayers: should be false when using a null for min players and negative for max players", gamesService.addGame(null,validName + System.nanoTime(), null, negative));
    }

    /**
     * Verifies that adding a game fails when the user and both player counts
     * are null.
     */
    @Test
    public void addGameNullUserValidGameNameNullMinPlayersNullMaxPlayers () {
        assertFalse("addGameNullUserValidGameNameNullMinPlayersNullMaxPlayers: should be false when using a null user", gamesService.addGame(null,validName + System.nanoTime(), null, null));
    }

    /**
     * Verifies that adding a game fails when the user and both player counts
     * are negative.
     */
    @Test
    public void addGameNullUserValidGameNameNegativeMinPlayersNegativeMaxPlayers () {
        assertFalse("addGameNullUserValidGameNameNegativeMinPlayersNegativeMaxPlayers: should be false when using a negative for min players and  max players", gamesService.addGame(null,validName + System.nanoTime(), negative, negative));
    }

    /**
     * Verifies that adding a game fails when the user is null and the
     * minimum and maximum player counts are equal.
     */
    @Test
    public void addGameNullUserValidGameNameMinPlayersEqualsMaxPlayers () {
        assertFalse("addGameNullUserValidGameNameMinPlayersEqualsMaxPlayers: should be false when using a null user", gamesService.addGame(null,validName + System.nanoTime(), smaller, smaller));
    }

    /**
     * Verifies that adding a game fails when the user is null and the
     * specified name already belongs to a public game.
     */
    @Test
    public void addGameNullUserInvalidGameNameValidMinPlayersNegativeMaxPlayers () {
        assertFalse("addGameNullUserInvalidGameNameValidMinPlayersNegativeMaxPlayers: should be false when using negative max players and invalid name", gamesService.addGame(null,invalidName, smaller, negative));
    }

    /**
     * Verifies that adding a game fails when the user is null, the game name
     * already belongs to a public game, and the minimum count is negative.
     */
    @Test
    public void addGameNullUserInvalidGameNameNegativeMinPlayersValidMaxPlayers () {
        assertFalse("addGameNullUserInvalidGameNameNegativeMinPlayersValidMaxPlayers: should be false when using negative min players and invalid name", gamesService.addGame(null,invalidName, negative, larger));
    }

    /**
     * Verifies that adding a game fails when the user is null and the minimum
     * player count exceeds the maximum for an existing public game name.
     */
    @Test
    public void addGameNullUserInvalidGameNameLargerMinPlayersSmallerMaxPlayers () {
        assertFalse("addGameNullUserInvalidGameNameLargerMinPlayersSmallerMaxPlayers: should be false when using a min player count larger than max players and invalid name", gamesService.addGame(null,invalidName, larger, smaller));
    }

    /**
     * Verifies that adding a game fails when the user is null and the
     * maximum player count is null for an existing public game name.
     */
    @Test
    public void addGameNullUserInvalidGameNameValidMinPlayersNullMaxPlayers () {
        assertFalse("addGameNullUserInvalidGameNameValidMinPlayersNullMaxPlayers: should be false when using invalid name with valid min players and null max players", gamesService.addGame(null,invalidName, smaller, null));
    }

    /**
     * Verifies that adding a game fails when the user is null, the game name
     * already belongs to a public game, and the minimum count is negative.
     */
    @Test
    public void addGameNullUserInvalidGameNameNegativeMinPlayersNullMaxPlayers () {
        assertFalse("addGameNullUserInvalidGameNameNegativeMinPlayersNullMaxPlayers: should be false when using a null for max players and negative for min players and using an invalid name", gamesService.addGame(null,invalidName, negative, null));
    }

    /**
     * Verifies that adding a game fails when the user is null and the
     * minimum player count is null for an existing public game name.
     */
    @Test
    public void addGameNullUserInvalidGameNameNullMinPlayersValidMaxPlayers () {
        assertFalse("addGameNullUserInvalidGameNameNullMinPlayersValidMaxPlayers: should be false an invalid name with null min players and valid max players", gamesService.addGame(null,invalidName, null, smaller));
    }

    /**
     * Verifies that adding a game fails when the user is null, the game name
     * already belongs to a public game, and the maximum count is negative.
     */
    @Test
    public void addGameNullUserInvalidGameNameNullMinPlayersNegativeMaxPlayers () {
        assertFalse("addGameNullUserInvalidGameNameNullMinPlayersNegativeMaxPlayers: should be false when using a null for min players and negative for max players and using an invalid name", gamesService.addGame(null,invalidName, null, negative));
    }

    /**
     * Verifies that adding a game fails when the user is null and both
     * player counts are null for an existing public game name.
     */
    @Test
    public void addGameNullUserInvalidGameNameNullMinPlayersNullMaxPlayers () {
        assertFalse("addGameNullUserInvalidGameNameNullMinPlayersNullMaxPlayers: should be false when using a null for min players and max players and using an invalid name", gamesService.addGame(null,invalidName, null, null));
    }

    /**
     * Verifies that adding a game fails when the user is null, the game name
     * already belongs to a public game, and both player counts are negative.
     */
    @Test
    public void addGameNullUserInvalidGameNameNegativeMinPlayersNegativeMaxPlayers () {
        assertFalse("addGameNullUserInvalidGameNameNegativeMinPlayersNegativeMaxPlayers: should be false when using a negative for min players and  max players and using an invalid name", gamesService.addGame(null,invalidName, negative, negative));
    }

    /**
     * Verifies that adding a game fails when the user is null and the
     * player counts are equal for an existing public game name.
     */
    @Test
    public void addGameNullUserInvalidGameNameMinPlayersEqualsMaxPlayers () {
        assertFalse("addGameNullUserInvalidGameNameMinPlayersEqualsMaxPlayers: should be false when using the same number for min players and  max players and using an invalid name", gamesService.addGame(null,invalidName, smaller, smaller));
    }

    /**
     * Verifies that adding a game fails when both the user and game name
     * are null and the maximum player count is negative.
     */
    @Test
    public void addGameNullUserNullGameNameValidMinPlayersNegativeMaxPlayers () {
        assertFalse("addGameNullUserNullGameNameValidMinPlayersNegativeMaxPlayers: should be false when using negative max players and a null name", gamesService.addGame(null,null, smaller, negative));
    }

    /**
     * Verifies that adding a game fails when both the user and game name
     * are null and the minimum player count is negative.
     */
    @Test
    public void addGameNullUserNullGameNameNegativeMinPlayersValidMaxPlayers () {
        assertFalse("addGameNullUserNullGameNameNegativeMinPlayersValidMaxPlayers: should be false when using negative min players and a null name", gamesService.addGame(null,null, negative, larger));
    }

    /**
     * Verifies that adding a game fails when both the user and game name
     * are null and the minimum count exceeds the maximum.
     */
    @Test
    public void addGameNullUserNullGameNameLargerMinPlayersSmallerMaxPlayers () {
        assertFalse("addGameNullUserNullGameNameLargerMinPlayersSmallerMaxPlayers: should be false when using a min player count larger than max players and a null name", gamesService.addGame(null,null, larger, smaller));
    }

    /**
     * Verifies that adding a game fails when both the user and game name
     * are null and the maximum player count is null.
     */
    @Test
    public void addGameNullUserNullGameNameValidMinPlayersNullMaxPlayers () {
        assertFalse("addGameNullUserNullGameNameValidMinPlayersNullMaxPlayers: should be false when using null name with valid min players and null max players", gamesService.addGame(null,null, smaller, null));
    }

    /**
     * Verifies that adding a game fails when both the user and game name
     * are null, the minimum player count is negative, and the maximum is null.
     */
    @Test
    public void addGameNullUserNullGameNameNegativeMinPlayersNullMaxPlayers () {
        assertFalse("addGameNullUserNullGameNameNegativeMinPlayersNullMaxPlayers: should be false when using a null for max players and negative for min players and using a null name", gamesService.addGame(null,null, negative, null));
    }

    /**
     * Verifies that adding a game fails when both the user and game name
     * are null, the minimum count is null, and the maximum count is valid.
     */
    @Test
    public void addGameNullUserNullGameNameNullMinPlayersValidMaxPlayers () {
        assertFalse("addGameNullUserNullGameNameNullMinPlayersValidMaxPlayers: should be false a null name with null min players and valid max players", gamesService.addGame(null,null, null, smaller));
    }

    /**
     * Verifies that adding a game fails when both the user and game name
     * are null, the minimum count is null, and the maximum count is negative.
     */
    @Test
    public void addGameNullUserNullGameNameNullMinPlayersNegativeMaxPlayers () {
        assertFalse("addGameNullUserNullGameNameNullMinPlayersNegativeMaxPlayers: should be false when using a null for min players and negative for max players and using a null name", gamesService.addGame(null,null, null, negative));
    }

    /**
     * Verifies that adding a game fails when the user and game name are null
     * and both player counts are null.
     */
    @Test
    public void addGameNullUserNullGameNameNullMinPlayersNullMaxPlayers () {
        assertFalse("addGameNullUserNullGameNameNullMinPlayersNullMaxPlayers: should be false when using a null for min players and max players and using a null name", gamesService.addGame(null,null, null, null));
    }

    /**
     * Verifies that adding a game fails when the user and game name are null
     * and both player counts are negative.
     */
    @Test
    public void addGameNullUserNullGameNameNegativeMinPlayersNegativeMaxPlayers () {
        assertFalse("addGameNullUserNullGameNameNegativeMinPlayersNegativeMaxPlayers: should be false when using a negative for min players and  max players and using a null name", gamesService.addGame(null,null, negative, negative));
    }

    /**
     * Verifies that adding a game fails when the user and game name are null
     * and the minimum and maximum player counts are equal.
     */
    @Test
    public void addGameNullUserNullGameNameMinPlayersEqualsMaxPlayers () {
        assertFalse("addGameNullUserNullGameNameMinPlayersEqualsMaxPlayers: should be false when using the same number for min players and  max players and using a null name", gamesService.addGame(null,null, smaller, smaller));
    }

}