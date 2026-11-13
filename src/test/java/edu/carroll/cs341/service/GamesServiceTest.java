package edu.carroll.cs341.service;

import edu.carroll.cs341.jpa.model.Game;
import edu.carroll.cs341.jpa.model.User;
import edu.carroll.cs341.jpa.repo.GamesRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.springframework.test.util.AssertionErrors.*;

/**
 * Tests the {@link GamesService} methods responsible for adding games.
 *
 * <p>Each test creates only the user and game fixtures it needs, then verifies
 * the expected results for valid and invalid inputs, including null values,
 * duplicate game names, negative player counts, and invalid player-count
 * ranges.</p>
 *
 * <p>The {@link SpringBootTest} annotation loads the Spring application
 * context so that the service and repository can be tested using their
 * configured dependencies. The {@link Transactional} annotation allows
 * database operations performed during each test to participate in a
 * transaction that is normally rolled back after the test completes.</p>
 *
 * <p>Codex assisted with writing the test class, it was given a format and then the worked was checked by the author.</p>
 *
 * @author Max Montowski
 */
@SpringBootTest
@Transactional
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
    private GamesOwnedService gamesOwnedService;

    @Autowired
    private GamesRepository gameRepo;

    @Autowired
    private UserService userService;

    /*
     * = = = = = = = = = = =
     * Valid User and Game Name Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that a game cannot be added when the maximum player count
     * is negative.
     */
    @Test
    public void addGameValidUserValidGameNameValidMinPlayersNegativeMaxPlayers() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        boolean result = gamesService.addGame(user, validName, smaller, negative);
        assertFalse("addGameValidUserValidGameNameValidMinPlayersNegativeMaxPlayers: should reject a negative maximum player count", result);
    }

    /**
     * Verifies that a game cannot be added when the minimum player count
     * is negative.
     */
    @Test
    public void addGameValidUserValidGameNameNegativeMinPlayersValidMaxPlayers() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        boolean result = gamesService.addGame(user, validName, negative, larger);
        assertFalse("addGameValidUserValidGameNameNegativeMinPlayersValidMaxPlayers: should reject a negative minimum player count", result);
    }

    /**
     * Verifies that a game cannot be added when the minimum player count
     * exceeds the maximum player count.
     */
    @Test
    public void addGameValidUserValidGameNameLargerMinPlayersSmallerMaxPlayers() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        boolean result = gamesService.addGame(user, validName, larger, smaller);
        assertFalse("addGameValidUserValidGameNameLargerMinPlayersSmallerMaxPlayers: should reject a minimum greater than the maximum", result);
    }

    /**
     * Verifies that a game can be added when the maximum player count is null.
     */
    @Test
    public void addGameValidUserValidGameNameValidMinPlayersNullMaxPlayers() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        boolean result = gamesService.addGame(user, validName, smaller, null);
        assertTrue("addGameValidUserValidGameNameValidMinPlayersNullMaxPlayers: should allow a null maximum player count", result);
    }

    /**
     * Verifies that a game cannot be added when the minimum player count
     * is negative and the maximum player count is null.
     */
    @Test
    public void addGameValidUserValidGameNameNegativeMinPlayersNullMaxPlayers() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        boolean result = gamesService.addGame(user, validName, negative, null);
        assertFalse("addGameValidUserValidGameNameNegativeMinPlayersNullMaxPlayers: should reject a negative minimum player count", result);
    }

    /**
     * Verifies that a game can be added when the minimum player count is null.
     */
    @Test
    public void addGameValidUserValidGameNameNullMinPlayersValidMaxPlayers() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        boolean result = gamesService.addGame(user, validName, null, smaller);
        assertTrue("addGameValidUserValidGameNameNullMinPlayersValidMaxPlayers: should allow a null minimum player count", result);
    }

    /**
     * Verifies that a game cannot be added when the maximum player count
     * is negative and the minimum player count is null.
     */
    @Test
    public void addGameValidUserValidGameNameNullMinPlayersNegativeMaxPlayers() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        boolean result = gamesService.addGame(user, validName, null, negative);
        assertFalse("addGameValidUserValidGameNameNullMinPlayersNegativeMaxPlayers: should reject a negative maximum player count", result);
    }

    /**
     * Verifies that a game can be added when both player counts are null.
     */
    @Test
    public void addGameValidUserValidGameNameNullMinPlayersNullMaxPlayers() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        boolean result = gamesService.addGame(user, validName, null, null);
        assertTrue("addGameValidUserValidGameNameNullMinPlayersNullMaxPlayers: should allow both player counts to be null", result);
    }

    /**
     * Verifies that a game cannot be added when both player counts are negative.
     */
    @Test
    public void addGameValidUserValidGameNameNegativeMinPlayersNegativeMaxPlayers() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        boolean result = gamesService.addGame(user, validName, negative, negative);
        assertFalse("addGameValidUserValidGameNameNegativeMinPlayersNegativeMaxPlayers: should reject negative player counts", result);
    }

    /**
     * Verifies that a game can be added when the minimum and maximum
     * player counts are equal.
     */
    @Test
    public void addGameValidUserValidGameNameMinPlayersEqualsMaxPlayers() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        boolean result = gamesService.addGame(user, validName, smaller, smaller);
        assertTrue("addGameValidUserValidGameNameMinPlayersEqualsMaxPlayers: should allow equal minimum and maximum player counts", result);
    }

    /*
     * = = = = = = = = = = =
     * Valid User and Existing Game Name Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that adding a game with an existing user-owned game name
     * fails when the maximum player count is negative.
     */
    @Test
    public void addGameValidUserInvalidGameNameValidMinPlayersNegativeMaxPlayers() {
        User user = insertTestingUserWithVisibleGame();
        boolean result = gamesService.addGame(user, invalidName2, smaller, negative);
        assertFalse("addGameValidUserInvalidGameNameValidMinPlayersNegativeMaxPlayers: should reject this duplicate-name and player-count combination", result);
    }

    /**
     * Verifies that adding a game with an existing user-owned game name
     * fails when the minimum player count is negative.
     */
    @Test
    public void addGameValidUserInvalidGameNameNegativeMinPlayersValidMaxPlayers() {
        User user = insertTestingUserWithVisibleGame();
        boolean result = gamesService.addGame(user, invalidName2, negative, larger);
        assertFalse("addGameValidUserInvalidGameNameNegativeMinPlayersValidMaxPlayers: should reject this duplicate-name and player-count combination", result);
    }

    /**
     * Verifies that adding a game with an existing user-owned game name
     * fails when the minimum player count exceeds the maximum.
     */
    @Test
    public void addGameValidUserInvalidGameNameLargerMinPlayersSmallerMaxPlayers() {
        User user = insertTestingUserWithVisibleGame();
        boolean result = gamesService.addGame(user, invalidName2, larger, smaller);
        assertFalse("addGameValidUserInvalidGameNameLargerMinPlayersSmallerMaxPlayers: should reject this duplicate-name and player-count combination", result);
    }

    /**
     * Verifies that adding a game with an existing user-owned game name
     * fails when the maximum player count is null.
     */
    @Test
    public void addGameValidUserInvalidGameNameValidMinPlayersNullMaxPlayers() {
        User user = insertTestingUserWithVisibleGame();
        boolean result = gamesService.addGame(user, invalidName2, smaller, null);
        assertFalse("addGameValidUserInvalidGameNameValidMinPlayersNullMaxPlayers: should reject an existing visible game name", result);
    }

    /**
     * Verifies that adding a game with an existing user-owned game name
     * fails when the minimum player count is negative and the maximum is null.
     */
    @Test
    public void addGameValidUserInvalidGameNameNegativeMinPlayersNullMaxPlayers() {
        User user = insertTestingUserWithVisibleGame();
        boolean result = gamesService.addGame(user, invalidName2, negative, null);
        assertFalse("addGameValidUserInvalidGameNameNegativeMinPlayersNullMaxPlayers: should reject this duplicate-name and player-count combination", result);
    }

    /**
     * Verifies that adding a game with an existing user-owned game name
     * fails when the minimum player count is null.
     */
    @Test
    public void addGameValidUserInvalidGameNameNullMinPlayersValidMaxPlayers() {
        User user = insertTestingUserWithVisibleGame();
        boolean result = gamesService.addGame(user, invalidName2, null, smaller);
        assertFalse("addGameValidUserInvalidGameNameNullMinPlayersValidMaxPlayers: should reject an existing visible game name", result);
    }

    /**
     * Verifies that adding a game with an existing user-owned game name
     * fails when the maximum player count is negative and the minimum is null.
     */
    @Test
    public void addGameValidUserInvalidGameNameNullMinPlayersNegativeMaxPlayers() {
        User user = insertTestingUserWithVisibleGame();
        boolean result = gamesService.addGame(user, invalidName2, null, negative);
        assertFalse("addGameValidUserInvalidGameNameNullMinPlayersNegativeMaxPlayers: should reject this duplicate-name and player-count combination", result);
    }

    /**
     * Verifies that adding a game with an existing user-owned game name
     * fails when both player counts are null.
     */
    @Test
    public void addGameValidUserInvalidGameNameNullMinPlayersNullMaxPlayers() {
        User user = insertTestingUserWithVisibleGame();
        boolean result = gamesService.addGame(user, invalidName2, null, null);
        assertFalse("addGameValidUserInvalidGameNameNullMinPlayersNullMaxPlayers: should reject an existing visible game name", result);
    }

    /**
     * Verifies that adding a game with an existing user-owned game name
     * fails when both player counts are negative.
     */
    @Test
    public void addGameValidUserInvalidGameNameNegativeMinPlayersNegativeMaxPlayers() {
        User user = insertTestingUserWithVisibleGame();
        boolean result = gamesService.addGame(user, invalidName2, negative, negative);
        assertFalse("addGameValidUserInvalidGameNameNegativeMinPlayersNegativeMaxPlayers: should reject this duplicate-name and player-count combination", result);
    }

    /**
     * Verifies that adding a game with an existing user-owned game name
     * fails when the minimum and maximum player counts are equal.
     */
    @Test
    public void addGameValidUserInvalidGameNameMinPlayersEqualsMaxPlayers() {
        User user = insertTestingUserWithVisibleGame();
        boolean result = gamesService.addGame(user, invalidName2, smaller, smaller);
        assertFalse("addGameValidUserInvalidGameNameMinPlayersEqualsMaxPlayers: should reject an existing visible game name", result);
    }

    /*
     * = = = = = = = = = = =
     * Valid User and Null Game Name Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that adding a game with a null name fails when the maximum
     * player count is negative.
     */
    @Test
    public void addGameValidUserNullGameNameValidMinPlayersNegativeMaxPlayers() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        boolean result = gamesService.addGame(user, null, smaller, negative);
        assertFalse("addGameValidUserNullGameNameValidMinPlayersNegativeMaxPlayers: should reject a null game name", result);
    }

    /**
     * Verifies that adding a game with a null name fails when the minimum
     * player count is negative.
     */
    @Test
    public void addGameValidUserNullGameNameNegativeMinPlayersValidMaxPlayers() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        boolean result = gamesService.addGame(user, null, negative, larger);
        assertFalse("addGameValidUserNullGameNameNegativeMinPlayersValidMaxPlayers: should reject a null game name", result);
    }

    /**
     * Verifies that adding a game with a null name fails when the minimum
     * player count exceeds the maximum.
     */
    @Test
    public void addGameValidUserNullGameNameLargerMinPlayersSmallerMaxPlayers() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        boolean result = gamesService.addGame(user, null, larger, smaller);
        assertFalse("addGameValidUserNullGameNameLargerMinPlayersSmallerMaxPlayers: should reject a null game name", result);
    }

    /**
     * Verifies that adding a game with a null name fails when the maximum
     * player count is null.
     */
    @Test
    public void addGameValidUserNullGameNameValidMinPlayersNullMaxPlayers() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        boolean result = gamesService.addGame(user, null, smaller, null);
        assertFalse("addGameValidUserNullGameNameValidMinPlayersNullMaxPlayers: should reject a null game name", result);
    }

    /**
     * Verifies that adding a game with a null name fails when the minimum
     * player count is negative and the maximum is null.
     */
    @Test
    public void addGameValidUserNullGameNameNegativeMinPlayersNullMaxPlayers() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        boolean result = gamesService.addGame(user, null, negative, null);
        assertFalse("addGameValidUserNullGameNameNegativeMinPlayersNullMaxPlayers: should reject a null game name", result);
    }

    /**
     * Verifies that adding a game with a null name fails when the minimum
     * player count is null and the maximum is valid.
     */
    @Test
    public void addGameValidUserNullGameNameNullMinPlayersValidMaxPlayers() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        boolean result = gamesService.addGame(user, null, null, smaller);
        assertFalse("addGameValidUserNullGameNameNullMinPlayersValidMaxPlayers: should reject a null game name", result);
    }

    /**
     * Verifies that adding a game with a null name fails when the minimum
     * player count is null and the maximum is negative.
     */
    @Test
    public void addGameValidUserNullGameNameNullMinPlayersNegativeMaxPlayers() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        boolean result = gamesService.addGame(user, null, null, negative);
        assertFalse("addGameValidUserNullGameNameNullMinPlayersNegativeMaxPlayers: should reject a null game name", result);
    }

    /**
     * Verifies that adding a game with a null name fails when both player
     * counts are null.
     */
    @Test
    public void addGameValidUserNullGameNameNullMinPlayersNullMaxPlayers() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        boolean result = gamesService.addGame(user, null, null, null);
        assertFalse("addGameValidUserNullGameNameNullMinPlayersNullMaxPlayers: should reject a null game name", result);
    }

    /**
     * Verifies that adding a game with a null name fails when both player
     * counts are negative.
     */
    @Test
    public void addGameValidUserNullGameNameNegativeMinPlayersNegativeMaxPlayers() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        boolean result = gamesService.addGame(user, null, negative, negative);
        assertFalse("addGameValidUserNullGameNameNegativeMinPlayersNegativeMaxPlayers: should reject a null game name", result);
    }

    /**
     * Verifies that adding a game with a null name fails when the minimum
     * and maximum player counts are equal.
     */
    @Test
    public void addGameValidUserNullGameNameMinPlayersEqualsMaxPlayers() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        boolean result = gamesService.addGame(user, null, smaller, smaller);
        assertFalse("addGameValidUserNullGameNameMinPlayersEqualsMaxPlayers: should reject a null game name", result);
    }

    /*
     * = = = = = = = = = = =
     * Null User and Valid Game Name Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that adding a game fails when the user is null and the
     * maximum player count is negative.
     */
    @Test
    public void addGameNullUserValidGameNameValidMinPlayersNegativeMaxPlayers() {
        boolean result = gamesService.addGame(null, validName, smaller, negative);
        assertFalse("addGameNullUserValidGameNameValidMinPlayersNegativeMaxPlayers: should reject a null user", result);
    }

    /**
     * Verifies that adding a game fails when the user is null and the
     * minimum player count is negative.
     */
    @Test
    public void addGameNullUserValidGameNameNegativeMinPlayersValidMaxPlayers() {
        boolean result = gamesService.addGame(null, validName, negative, larger);
        assertFalse("addGameNullUserValidGameNameNegativeMinPlayersValidMaxPlayers: should reject a null user", result);
    }

    /**
     * Verifies that adding a game fails when the user is null and the
     * minimum player count exceeds the maximum.
     */
    @Test
    public void addGameNullUserValidGameNameLargerMinPlayersSmallerMaxPlayers() {
        boolean result = gamesService.addGame(null, validName, larger, smaller);
        assertFalse("addGameNullUserValidGameNameLargerMinPlayersSmallerMaxPlayers: should reject a null user", result);
    }

    /**
     * Verifies that adding a game fails when the user is null and the
     * maximum player count is null.
     */
    @Test
    public void addGameNullUserValidGameNameValidMinPlayersNullMaxPlayers() {
        boolean result = gamesService.addGame(null, validName, smaller, null);
        assertFalse("addGameNullUserValidGameNameValidMinPlayersNullMaxPlayers: should reject a null user", result);
    }

    /**
     * Verifies that adding a game fails when the user is null, the minimum
     * player count is negative, and the maximum is null.
     */
    @Test
    public void addGameNullUserValidGameNameNegativeMinPlayersNullMaxPlayers() {
        boolean result = gamesService.addGame(null, validName, negative, null);
        assertFalse("addGameNullUserValidGameNameNegativeMinPlayersNullMaxPlayers: should reject a null user", result);
    }

    /**
     * Verifies that adding a game fails when the user is null and the
     * minimum player count is null.
     */
    @Test
    public void addGameNullUserValidGameNameNullMinPlayersValidMaxPlayers() {
        boolean result = gamesService.addGame(null, validName, null, smaller);
        assertFalse("addGameNullUserValidGameNameNullMinPlayersValidMaxPlayers: should reject a null user", result);
    }

    /**
     * Verifies that adding a game fails when the user is null, the minimum
     * player count is null, and the maximum is negative.
     */
    @Test
    public void addGameNullUserValidGameNameNullMinPlayersNegativeMaxPlayers() {
        boolean result = gamesService.addGame(null, validName, null, negative);
        assertFalse("addGameNullUserValidGameNameNullMinPlayersNegativeMaxPlayers: should reject a null user", result);
    }

    /**
     * Verifies that adding a game fails when the user and both player counts
     * are null.
     */
    @Test
    public void addGameNullUserValidGameNameNullMinPlayersNullMaxPlayers() {
        boolean result = gamesService.addGame(null, validName, null, null);
        assertFalse("addGameNullUserValidGameNameNullMinPlayersNullMaxPlayers: should reject a null user", result);
    }

    /**
     * Verifies that adding a game fails when the user and both player counts
     * are negative.
     */
    @Test
    public void addGameNullUserValidGameNameNegativeMinPlayersNegativeMaxPlayers() {
        boolean result = gamesService.addGame(null, validName, negative, negative);
        assertFalse("addGameNullUserValidGameNameNegativeMinPlayersNegativeMaxPlayers: should reject a null user", result);
    }

    /**
     * Verifies that adding a game fails when the user is null and the
     * minimum and maximum player counts are equal.
     */
    @Test
    public void addGameNullUserValidGameNameMinPlayersEqualsMaxPlayers() {
        boolean result = gamesService.addGame(null, validName, smaller, smaller);
        assertFalse("addGameNullUserValidGameNameMinPlayersEqualsMaxPlayers: should reject a null user", result);
    }

    /*
     * = = = = = = = = = = =
     * Null User and Existing Public Game Name Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that a null user is rejected even when the requested name
     * belongs to a public game.
     */
    @Test
    public void addGameNullUserInvalidGameNameValidMinPlayersNegativeMaxPlayers() {
        insertTestingPublicGame();
        boolean result = gamesService.addGame(null, invalidName, smaller, negative);
        assertFalse("addGameNullUserInvalidGameNameValidMinPlayersNegativeMaxPlayers: should reject a null user before checking the public game name", result);
    }

    /**
     * Verifies that a null user is rejected even when the requested name
     * belongs to a public game.
     */
    @Test
    public void addGameNullUserInvalidGameNameNegativeMinPlayersValidMaxPlayers() {
        insertTestingPublicGame();
        boolean result = gamesService.addGame(null, invalidName, negative, larger);
        assertFalse("addGameNullUserInvalidGameNameNegativeMinPlayersValidMaxPlayers: should reject a null user before checking the public game name", result);
    }

    /**
     * Verifies that a null user is rejected even when the requested name
     * belongs to a public game.
     */
    @Test
    public void addGameNullUserInvalidGameNameLargerMinPlayersSmallerMaxPlayers() {
        insertTestingPublicGame();
        boolean result = gamesService.addGame(null, invalidName, larger, smaller);
        assertFalse("addGameNullUserInvalidGameNameLargerMinPlayersSmallerMaxPlayers: should reject a null user before checking the public game name", result);
    }

    /**
     * Verifies that a null user is rejected even when the requested name
     * belongs to a public game.
     */
    @Test
    public void addGameNullUserInvalidGameNameValidMinPlayersNullMaxPlayers() {
        insertTestingPublicGame();
        boolean result = gamesService.addGame(null, invalidName, smaller, null);
        assertFalse("addGameNullUserInvalidGameNameValidMinPlayersNullMaxPlayers: should reject a null user before checking the public game name", result);
    }

    /**
     * Verifies that a null user is rejected even when the requested name
     * belongs to a public game.
     */
    @Test
    public void addGameNullUserInvalidGameNameNegativeMinPlayersNullMaxPlayers() {
        insertTestingPublicGame();
        boolean result = gamesService.addGame(null, invalidName, negative, null);
        assertFalse("addGameNullUserInvalidGameNameNegativeMinPlayersNullMaxPlayers: should reject a null user before checking the public game name", result);
    }

    /**
     * Verifies that a null user is rejected even when the requested name
     * belongs to a public game.
     */
    @Test
    public void addGameNullUserInvalidGameNameNullMinPlayersValidMaxPlayers() {
        insertTestingPublicGame();
        boolean result = gamesService.addGame(null, invalidName, null, smaller);
        assertFalse("addGameNullUserInvalidGameNameNullMinPlayersValidMaxPlayers: should reject a null user before checking the public game name", result);
    }

    /**
     * Verifies that a null user is rejected even when the requested name
     * belongs to a public game.
     */
    @Test
    public void addGameNullUserInvalidGameNameNullMinPlayersNegativeMaxPlayers() {
        insertTestingPublicGame();
        boolean result = gamesService.addGame(null, invalidName, null, negative);
        assertFalse("addGameNullUserInvalidGameNameNullMinPlayersNegativeMaxPlayers: should reject a null user before checking the public game name", result);
    }

    /**
     * Verifies that a null user is rejected even when the requested name
     * belongs to a public game.
     */
    @Test
    public void addGameNullUserInvalidGameNameNullMinPlayersNullMaxPlayers() {
        insertTestingPublicGame();
        boolean result = gamesService.addGame(null, invalidName, null, null);
        assertFalse("addGameNullUserInvalidGameNameNullMinPlayersNullMaxPlayers: should reject a null user before checking the public game name", result);
    }

    /**
     * Verifies that a null user is rejected even when the requested name
     * belongs to a public game.
     */
    @Test
    public void addGameNullUserInvalidGameNameNegativeMinPlayersNegativeMaxPlayers() {
        insertTestingPublicGame();
        boolean result = gamesService.addGame(null, invalidName, negative, negative);
        assertFalse("addGameNullUserInvalidGameNameNegativeMinPlayersNegativeMaxPlayers: should reject a null user before checking the public game name", result);
    }

    /**
     * Verifies that a null user is rejected even when the requested name
     * belongs to a public game.
     */
    @Test
    public void addGameNullUserInvalidGameNameMinPlayersEqualsMaxPlayers() {
        insertTestingPublicGame();
        boolean result = gamesService.addGame(null, invalidName, smaller, smaller);
        assertFalse("addGameNullUserInvalidGameNameMinPlayersEqualsMaxPlayers: should reject a null user before checking the public game name", result);
    }

    /*
     * = = = = = = = = = = =
     * Null User and Null Game Name Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that adding a game fails when both the user and game name
     * are null and the maximum player count is negative.
     */
    @Test
    public void addGameNullUserNullGameNameValidMinPlayersNegativeMaxPlayers() {
        boolean result = gamesService.addGame(null, null, smaller, negative);
        assertFalse("addGameNullUserNullGameNameValidMinPlayersNegativeMaxPlayers: should reject a null user", result);
    }

    /**
     * Verifies that adding a game fails when both the user and game name
     * are null and the minimum player count is negative.
     */
    @Test
    public void addGameNullUserNullGameNameNegativeMinPlayersValidMaxPlayers() {
        boolean result = gamesService.addGame(null, null, negative, larger);
        assertFalse("addGameNullUserNullGameNameNegativeMinPlayersValidMaxPlayers: should reject a null user", result);
    }

    /**
     * Verifies that adding a game fails when both the user and game name
     * are null and the minimum count exceeds the maximum.
     */
    @Test
    public void addGameNullUserNullGameNameLargerMinPlayersSmallerMaxPlayers() {
        boolean result = gamesService.addGame(null, null, larger, smaller);
        assertFalse("addGameNullUserNullGameNameLargerMinPlayersSmallerMaxPlayers: should reject a null user", result);
    }

    /**
     * Verifies that adding a game fails when both the user and game name
     * are null and the maximum player count is null.
     */
    @Test
    public void addGameNullUserNullGameNameValidMinPlayersNullMaxPlayers() {
        boolean result = gamesService.addGame(null, null, smaller, null);
        assertFalse("addGameNullUserNullGameNameValidMinPlayersNullMaxPlayers: should reject a null user", result);
    }

    /**
     * Verifies that adding a game fails when both the user and game name
     * are null, the minimum player count is negative, and the maximum is null.
     */
    @Test
    public void addGameNullUserNullGameNameNegativeMinPlayersNullMaxPlayers() {
        boolean result = gamesService.addGame(null, null, negative, null);
        assertFalse("addGameNullUserNullGameNameNegativeMinPlayersNullMaxPlayers: should reject a null user", result);
    }

    /**
     * Verifies that adding a game fails when both the user and game name
     * are null, the minimum count is null, and the maximum count is valid.
     */
    @Test
    public void addGameNullUserNullGameNameNullMinPlayersValidMaxPlayers() {
        boolean result = gamesService.addGame(null, null, null, smaller);
        assertFalse("addGameNullUserNullGameNameNullMinPlayersValidMaxPlayers: should reject a null user", result);
    }

    /**
     * Verifies that adding a game fails when both the user and game name
     * are null, the minimum count is null, and the maximum count is negative.
     */
    @Test
    public void addGameNullUserNullGameNameNullMinPlayersNegativeMaxPlayers() {
        boolean result = gamesService.addGame(null, null, null, negative);
        assertFalse("addGameNullUserNullGameNameNullMinPlayersNegativeMaxPlayers: should reject a null user", result);
    }

    /**
     * Verifies that adding a game fails when the user and game name are null
     * and both player counts are null.
     */
    @Test
    public void addGameNullUserNullGameNameNullMinPlayersNullMaxPlayers() {
        boolean result = gamesService.addGame(null, null, null, null);
        assertFalse("addGameNullUserNullGameNameNullMinPlayersNullMaxPlayers: should reject a null user", result);
    }

    /**
     * Verifies that adding a game fails when the user and game name are null
     * and both player counts are negative.
     */
    @Test
    public void addGameNullUserNullGameNameNegativeMinPlayersNegativeMaxPlayers() {
        boolean result = gamesService.addGame(null, null, negative, negative);
        assertFalse("addGameNullUserNullGameNameNegativeMinPlayersNegativeMaxPlayers: should reject a null user", result);
    }

    /**
     * Verifies that adding a game fails when the user and game name are null
     * and the minimum and maximum player counts are equal.
     */
    @Test
    public void addGameNullUserNullGameNameMinPlayersEqualsMaxPlayers() {
        boolean result = gamesService.addGame(null, null, smaller, smaller);
        assertFalse("addGameNullUserNullGameNameMinPlayersEqualsMaxPlayers: should reject a null user", result);
    }

    /*
     * = = = = = = = = = = =
     * Get Available Games Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that a null user receives an empty list.
     */
    @Test
    public void getAvailableGamesNullUserTest() {
        List<Game> result = gamesService.getAvailableGames(null);

        assertNotNull("getAvailableGamesNullUserTest: should return a list", result);
        assertTrue("getAvailableGamesNullUserTest: should return an empty list",
                result.isEmpty());
    }

    /**
     * Verifies that public games and the user's visible games are included,
     * while hidden games and another user's games are excluded.
     */
    @Test
    public void getAvailableGamesVisibleGamesTest() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        User otherUser = userService.newUser("otheruser", password, password);

        Game publicGame = insertTestingGame("Public Game", null, true);
        Game userGame = insertTestingGame("User Game", user, true);
        Game hiddenGame = insertTestingGame("Hidden Game", user, false);
        Game otherGame = insertTestingGame("Other Game", otherUser, true);

        List<Game> result = gamesService.getAvailableGames(user);

        assertTrue("getAvailableGamesVisibleGamesTest: should include public games",
                result.contains(publicGame));
        assertTrue("getAvailableGamesVisibleGamesTest: should include the user's visible games",
                result.contains(userGame));
        assertFalse("getAvailableGamesVisibleGamesTest: should exclude hidden games",
                result.contains(hiddenGame));
        assertFalse("getAvailableGamesVisibleGamesTest: should exclude another user's games",
                result.contains(otherGame));
    }

    /**
     * Verifies that a user's version replaces the public game of the same name.
     */
    @Test
    public void getAvailableGamesUserCopyOverridesPublicGameTest() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        Game publicGame = insertTestingGame("Shared Game", null, true);
        Game userGame = insertTestingGame("Shared Game", user, true);

        List<Game> result = gamesService.getAvailableGames(user);

        assertTrue("getAvailableGamesUserCopyOverridesPublicGameTest: should include the user's copy",
                result.contains(userGame));
        assertFalse("getAvailableGamesUserCopyOverridesPublicGameTest: should exclude the public copy",
                result.contains(publicGame));
    }

    /**
     * Verifies that a hidden user copy also hides the corresponding public game.
     */
    @Test
    public void getAvailableGamesHiddenCopyHidesPublicGameTest() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        Game publicGame = insertTestingGame("Shared Game", null, true);
        Game hiddenGame = insertTestingGame("Shared Game", user, false);

        List<Game> result = gamesService.getAvailableGames(user);

        assertFalse("getAvailableGamesHiddenCopyHidesPublicGameTest: should exclude the public copy",
                result.contains(publicGame));
        assertFalse("getAvailableGamesHiddenCopyHidesPublicGameTest: should exclude the hidden copy",
                result.contains(hiddenGame));
    }

    /**
     * Verifies that available games are sorted alphabetically without
     * treating uppercase and lowercase letters differently.
     */
    @Test
    public void getAvailableGamesAlphabeticalOrderTest() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        Game zebra = insertTestingGame("zebra", user, true);
        Game apple = insertTestingGame("apple", user, true);
        Game banana = insertTestingGame("Banana", user, true);

        List<Game> result = gamesService.getAvailableGames(user);

        assertTrue("getAvailableGamesAlphabeticalOrderTest: should include all three games",
                result.contains(apple) && result.contains(banana) && result.contains(zebra));
        assertTrue("getAvailableGamesAlphabeticalOrderTest: apple should precede Banana",
                result.indexOf(apple) < result.indexOf(banana));
        assertTrue("getAvailableGamesAlphabeticalOrderTest: Banana should precede zebra",
                result.indexOf(banana) < result.indexOf(zebra));
    }

    /*
     * = = = = = = = = = = =
     * Get Game By Game ID Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that an existing game can be retrieved by its ID.
     */
    @Test
    public void getGameByGameIDSuccessTest() {
        Game game = insertTestingGame(validName, null, true);

        Game result = gamesService.getGameByGameID(game.getGameID());

        assertNotNull("getGameByGameIDSuccessTest: should find the game", result);
        assertEquals("getGameByGameIDSuccessTest: should return the matching game",
                game.getGameID(), result.getGameID());
    }

    /**
     * Verifies that a null game ID returns null.
     */
    @Test
    public void getGameByGameIDNullIDTest() {
        Game result = gamesService.getGameByGameID(null);

        assertNull("getGameByGameIDNullIDTest: should return null", result);
    }

    /**
     * Verifies that a nonexistent game ID returns null.
     */
    @Test
    public void getGameByGameIDInvalidIDTest() {
        Game result = gamesService.getGameByGameID(-1L);

        assertNull("getGameByGameIDInvalidIDTest: should return null", result);
    }

    /*
     * = = = = = = = = = = =
     * Edit Game Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that a user's game can be updated without changing its name.
     */
    @Test
    public void editGameSameNameSuccessTest() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        Game game = insertTestingGame(validName, user, true);

        boolean result = gamesService.editGame(
                user, game.getGameID(), validName, 2, 6);

        assertTrue("editGameSameNameSuccessTest: should update the game", result);

        Game updatedGame = gamesService.getGameByGameID(game.getGameID());

        assertEquals("editGameSameNameSuccessTest: should update minimum players",
                Integer.valueOf(2), updatedGame.getMinPlayers());
        assertEquals("editGameSameNameSuccessTest: should update maximum players",
                Integer.valueOf(6), updatedGame.getMaxPlayers());
        assertTrue("editGameSameNameSuccessTest: should keep the game visible",
                updatedGame.getCanSee());
    }

    /**
     * Verifies that editing a public game creates a user copy and preserves
     * the public game's player counts.
     */
    @Test
    public void editGamePublicGameCreatesUserCopyTest() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        Game publicGame = insertTestingGame(validName, null, true);

        boolean result = gamesService.editGame(
                user, publicGame.getGameID(), validName, 2, 6);

        assertTrue("editGamePublicGameCreatesUserCopyTest: should edit the game", result);

        Game userCopy = gameRepo.findByGameNameIgnoreCaseAndUserWhoAdded(
                validName, user);

        assertNotNull("editGamePublicGameCreatesUserCopyTest: should create a user copy",
                userCopy);
        assertNotEquals("editGamePublicGameCreatesUserCopyTest: should create a separate record",
                publicGame.getGameID(), userCopy.getGameID());
        assertEquals("editGamePublicGameCreatesUserCopyTest: should set the user's minimum",
                Integer.valueOf(2), userCopy.getMinPlayers());
        assertEquals("editGamePublicGameCreatesUserCopyTest: should set the user's maximum",
                Integer.valueOf(6), userCopy.getMaxPlayers());
        assertEquals("editGamePublicGameCreatesUserCopyTest: should preserve the public minimum",
                smaller, publicGame.getMinPlayers());
        assertEquals("editGamePublicGameCreatesUserCopyTest: should preserve the public maximum",
                larger, publicGame.getMaxPlayers());
    }

    /**
     * Verifies that renaming a user's game hides the original and creates
     * a visible game with the new name.
     */
    @Test
    public void editGameNewNameSuccessTest() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        Game game = insertTestingGame(validName, user, true);

        boolean result = gamesService.editGame(
                user, game.getGameID(), "Renamed Game", 2, 6);

        assertTrue("editGameNewNameSuccessTest: should rename the game", result);

        Game renamedGame = gameRepo.findByGameNameIgnoreCaseAndUserWhoAdded(
                "Renamed Game", user);

        assertNotNull("editGameNewNameSuccessTest: should save the renamed game",
                renamedGame);
        assertTrue("editGameNewNameSuccessTest: should make the renamed game visible",
                renamedGame.getCanSee());
        assertFalse("editGameNewNameSuccessTest: should hide the original game",
                gamesService.getGameByGameID(game.getGameID()).getCanSee());
        assertEquals("editGameNewNameSuccessTest: should save minimum players",
                Integer.valueOf(2), renamedGame.getMinPlayers());
        assertEquals("editGameNewNameSuccessTest: should save maximum players",
                Integer.valueOf(6), renamedGame.getMaxPlayers());
    }

    /**
     * Verifies that spaces around an updated game name are removed.
     */
    @Test
    public void editGameNameWithSpacesTest() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        Game game = insertTestingGame(validName, user, true);

        boolean result = gamesService.editGame(
                user, game.getGameID(), "  Renamed Game  ", smaller, larger);

        assertTrue("editGameNameWithSpacesTest: should accept the trimmed name", result);
        assertNotNull("editGameNameWithSpacesTest: should save the trimmed name",
                gameRepo.findByGameNameIgnoreCaseAndUserWhoAdded("Renamed Game", user));
    }

    /**
     * Verifies that editing fails when the user is null.
     */
    @Test
    public void editGameNullUserTest() {
        Game game = insertTestingGame(validName, null, true);

        boolean result = gamesService.editGame(
                null, game.getGameID(), "Renamed Game", smaller, larger);

        assertFalse("editGameNullUserTest: should reject a null user", result);
        assertEquals("editGameNullUserTest: should preserve the original name",
                validName, game.getGameName());
    }

    /**
     * Verifies that editing fails when the game ID is null.
     */
    @Test
    public void editGameNullGameIDTest() {
        insertTestingUser();
        User user = userService.validateUser(username, password);

        boolean result = gamesService.editGame(
                user, null, validName, smaller, larger);

        assertFalse("editGameNullGameIDTest: should reject a null game ID", result);
    }

    /**
     * Verifies that editing fails when the game does not exist.
     */
    @Test
    public void editGameInvalidGameIDTest() {
        insertTestingUser();
        User user = userService.validateUser(username, password);

        boolean result = gamesService.editGame(
                user, -1L, validName, smaller, larger);

        assertFalse("editGameInvalidGameIDTest: should reject a nonexistent game", result);
    }

    /**
     * Verifies that editing fails when the new game name is null.
     */
    @Test
    public void editGameNullGameNameTest() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        Game game = insertTestingGame(validName, user, true);

        boolean result = gamesService.editGame(
                user, game.getGameID(), null, smaller, larger);

        assertFalse("editGameNullGameNameTest: should reject a null name", result);
    }

    /**
     * Verifies that editing fails when the new game name is empty.
     */
    @Test
    public void editGameEmptyGameNameTest() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        Game game = insertTestingGame(validName, user, true);

        boolean result = gamesService.editGame(
                user, game.getGameID(), "", smaller, larger);

        assertFalse("editGameEmptyGameNameTest: should reject an empty name", result);
    }

    /**
     * Verifies that editing fails when the new name contains only spaces.
     */
    @Test
    public void editGameWhitespaceGameNameTest() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        Game game = insertTestingGame(validName, user, true);

        boolean result = gamesService.editGame(
                user, game.getGameID(), "   ", smaller, larger);

        assertFalse("editGameWhitespaceGameNameTest: should reject a blank name", result);
    }

    /**
     * Verifies that a game cannot be renamed to another visible game
     * belonging to the same user, regardless of capitalization.
     */
    @Test
    public void editGameExistingUserGameNameTest() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        Game game = insertTestingGame(validName, user, true);
        insertTestingGame("Existing Game", user, true);

        boolean result = gamesService.editGame(
                user, game.getGameID(), "EXISTING GAME", smaller, larger);

        assertFalse("editGameExistingUserGameNameTest: should reject a duplicate name",
                result);
        assertTrue("editGameExistingUserGameNameTest: should keep the original visible",
                game.getCanSee());
    }

    /**
     * Verifies that a game cannot be renamed to an existing public game.
     */
    @Test
    public void editGameExistingPublicGameNameTest() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        Game game = insertTestingGame(validName, user, true);
        insertTestingGame("Public Game", null, true);

        boolean result = gamesService.editGame(
                user, game.getGameID(), "Public Game", smaller, larger);

        assertFalse("editGameExistingPublicGameNameTest: should reject a public duplicate",
                result);
        assertTrue("editGameExistingPublicGameNameTest: should keep the original visible",
                game.getCanSee());
    }

    /**
     * Verifies that editing a public game updates this user's collection
     * references while preserving another user's references.
     */
    @Test
    public void editGameUpdatesCollectionReferencesTest() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        User otherUser = userService.newUser(
                "otheruser", password, password);
        Game publicGame = insertTestingGame(validName, null, true);

        assertTrue("editGameUpdatesCollectionReferencesTest: should add the user's copy",
                gamesOwnedService.addGameToCollection(user, publicGame.getGameID(), 2020));
        assertTrue("editGameUpdatesCollectionReferencesTest: should add the other user's copy",
                gamesOwnedService.addGameToCollection(otherUser, publicGame.getGameID(), 2021));

        boolean result = gamesService.editGame(
                user, publicGame.getGameID(), validName, 2, 6);

        assertTrue("editGameUpdatesCollectionReferencesTest: should edit successfully",
                result);

        Game userCopy = gameRepo.findByGameNameIgnoreCaseAndUserWhoAdded(
                validName, user);

        assertNotNull("editGameUpdatesCollectionReferencesTest: should create a user copy",
                userCopy);
        assertEquals("editGameUpdatesCollectionReferencesTest: should update the user's reference",
                userCopy.getGameID(),
                gamesOwnedService.getGamesOwnedByUsername(user)
                        .getFirst().getGame().getGameID());
        assertEquals("editGameUpdatesCollectionReferencesTest: should preserve another user's reference",
                publicGame.getGameID(),
                gamesOwnedService.getGamesOwnedByUsername(otherUser)
                        .getFirst().getGame().getGameID());
    }

    /*
     * = = = = = = = = = = =
     * Remove Game Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that removing a user's game hides it without deleting its record.
     */
    @Test
    public void removeGameUserGameSuccessTest() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        Game game = insertTestingGame(validName, user, true);

        boolean result = gamesService.removeGame(user, game.getGameID());

        assertTrue("removeGameUserGameSuccessTest: should remove the game", result);

        Game removedGame = gamesService.getGameByGameID(game.getGameID());

        assertNotNull("removeGameUserGameSuccessTest: should preserve the database record",
                removedGame);
        assertFalse("removeGameUserGameSuccessTest: should hide the game",
                removedGame.getCanSee());
        assertFalse("removeGameUserGameSuccessTest: should exclude it from available games",
                gamesService.getAvailableGames(user).contains(removedGame));
    }

    /**
     * Verifies that removing a public game hides it for the requesting user
     * while keeping it available to other users.
     */
    @Test
    public void removeGamePublicGameSuccessTest() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        User otherUser = userService.newUser(
                "otheruser", password, password);
        Game publicGame = insertTestingGame(validName, null, true);

        boolean result = gamesService.removeGame(user, publicGame.getGameID());

        assertTrue("removeGamePublicGameSuccessTest: should remove the game", result);

        Game hiddenCopy = gameRepo.findByGameNameIgnoreCaseAndUserWhoAdded(
                validName, user);

        assertNotNull("removeGamePublicGameSuccessTest: should create a user copy",
                hiddenCopy);
        assertFalse("removeGamePublicGameSuccessTest: should hide the user copy",
                hiddenCopy.getCanSee());
        assertTrue("removeGamePublicGameSuccessTest: should preserve the public game",
                publicGame.getCanSee());
        assertFalse("removeGamePublicGameSuccessTest: should hide the public game for this user",
                gamesService.getAvailableGames(user).contains(publicGame));
        assertTrue("removeGamePublicGameSuccessTest: should remain available to another user",
                gamesService.getAvailableGames(otherUser).contains(publicGame));
    }

    /**
     * Verifies that removing a game fails when the user is null.
     */
    @Test
    public void removeGameNullUserTest() {
        Game game = insertTestingGame(validName, null, true);

        boolean result = gamesService.removeGame(null, game.getGameID());

        assertFalse("removeGameNullUserTest: should reject a null user", result);
        assertTrue("removeGameNullUserTest: should keep the game visible",
                game.getCanSee());
    }

    /**
     * Verifies that removing a game fails when the game ID is null.
     */
    @Test
    public void removeGameNullGameIDTest() {
        insertTestingUser();
        User user = userService.validateUser(username, password);

        boolean result = gamesService.removeGame(user, null);

        assertFalse("removeGameNullGameIDTest: should reject a null game ID", result);
    }

    /**
     * Verifies that removing a game fails when the game does not exist.
     */
    @Test
    public void removeGameInvalidGameIDTest() {
        insertTestingUser();
        User user = userService.validateUser(username, password);

        boolean result = gamesService.removeGame(user, -1L);

        assertFalse("removeGameInvalidGameIDTest: should reject a nonexistent game",
                result);
    }

    /**
     * Verifies that a game cannot be removed while it is referenced
     * by the user's collection.
     */
    @Test
    public void removeGameReferencedByCollectionTest() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        Game game = insertTestingGame(validName, user, true);

        assertTrue("removeGameReferencedByCollectionTest: should add the game to the collection",
                gamesOwnedService.addGameToCollection(user, game.getGameID(), 2020));

        boolean result = gamesService.removeGame(user, game.getGameID());

        assertFalse("removeGameReferencedByCollectionTest: should reject a referenced game",
                result);
        assertTrue("removeGameReferencedByCollectionTest: should keep the game visible",
                gamesService.getGameByGameID(game.getGameID()).getCanSee());
    }

    /**
     * Creates a game with explicit visibility and known player counts.
     * A null user creates a public game.
     */
    private Game insertTestingGame(String gameName, User user, boolean canSee) {
        Game game = new Game(gameName);
        game.setUserWhoAdded(user);
        game.setCanSee(canSee);
        game.setMinPlayers(smaller);
        game.setMaxPlayers(larger);
        return gameRepo.save(game);
    }

    private void insertTestingUser() {
        userService.newUser(username, password, password);
    }

    private User insertTestingUserWithVisibleGame() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        Game game = new Game(invalidName2);
        game.setUserWhoAdded(user);
        game.setCanSee(true);
        gameRepo.save(game);
        return user;
    }

    private void insertTestingPublicGame() {
        gameRepo.save(new Game(invalidName));
    }

}