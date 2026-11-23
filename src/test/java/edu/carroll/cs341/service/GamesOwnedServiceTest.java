package edu.carroll.cs341.service;

import edu.carroll.cs341.jpa.model.Family;
import edu.carroll.cs341.jpa.model.FamilyMembership;
import edu.carroll.cs341.jpa.model.Game;
import edu.carroll.cs341.jpa.model.GameOwned;
import edu.carroll.cs341.jpa.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Year;
import java.util.List;

import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertFalse;
import static org.springframework.test.util.AssertionErrors.assertNotNull;
import static org.springframework.test.util.AssertionErrors.assertNull;
import static org.springframework.test.util.AssertionErrors.assertTrue;

/**
 * Tests the {@link GamesOwnedService} methods responsible for managing users'
 * game collections and retrieving the games owned by a family.
 *
 * <p>The {@link SpringBootTest} annotation loads the Spring application
 * context so that the services use their configured dependencies. The
 * {@link Transactional} annotation rolls back database changes after each
 * test.</p>
 *
 * <p><b>Sources Used:</b></p>
 * <ul>
 *   <li>ChatGPT – Assisted with generating Javadoc comments and tests for this file
 *   in conjunction with Max Montowski.
 *   All comments were reviewed by the author and tests followed authors layout.</li>
 * </ul>
 */
@SpringBootTest
@Transactional
public class GamesOwnedServiceTest {

    @Autowired
    private GamesOwnedService gamesOwnedService;

    @Autowired
    private UserService userService;

    @Autowired
    private GamesService gamesService;

    @Autowired
    private FamilyService familyService;

    @Autowired
    private FamilyMembershipService membershipService;

    @PersistenceContext
    private EntityManager entityManager;

    /*
     * = = = = = = = = = = =
     * Get Games Owned Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that retrieving a user's owned games returns a list.
     */
    @Test
    public void getGamesOwnedByUsernameReturnsListTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        User user = insertTestingUser(username, password);
        Game game = insertTestingPublicGame("Owned Test Game");
        insertTestingOwnedGame(user, game, null);

        List<GameOwned> result = gamesOwnedService.getGamesOwnedByUsername(user);

        assertNotNull("getGamesOwnedByUsernameReturnsListTest: result should not be null", result);
        int gameCount = result.size();
        assertEquals("getGamesOwnedByUsernameReturnsListTest: result should contain one game", 1, gameCount);
        GameOwned firstCopy = result.getFirst();
        Game ownedGame = firstCopy.getGame();
        assertEquals("getGamesOwnedByUsernameReturnsListTest: result should contain the user's game", game, ownedGame);

        // Two owned copies, one per user
        User otherUser = insertTestingUser("othercollector", password);
        insertTestingOwnedGame(otherUser, game, null);

        result = gamesOwnedService.getGamesOwnedByUsername(user);
        gameCount = result.size();
        assertEquals("getGamesOwnedByUsernameReturnsListTest: first user should still see one copy", 1, gameCount);
        Long firstCopyID = result.getFirst().getGameOwnedID();
        assertEquals("getGamesOwnedByUsernameReturnsListTest: first user should still see the original copy",
                firstCopy.getGameOwnedID(), firstCopyID);

        result = gamesOwnedService.getGamesOwnedByUsername(otherUser);
        gameCount = result.size();
        assertEquals("getGamesOwnedByUsernameReturnsListTest: second user should see one copy", 1, gameCount);
        GameOwned secondCopy = result.getFirst();
        ownedGame = secondCopy.getGame();
        assertEquals("getGamesOwnedByUsernameReturnsListTest: second copy should point to the same game", game, ownedGame);
        boolean sameCopy = firstCopyID.equals(secondCopy.getGameOwnedID());
        assertFalse("getGamesOwnedByUsernameReturnsListTest: the copies should have different IDs", sameCopy);
    }

    /**
     * Verifies that retrieving games for a user without owned games
     * returns an empty list.
     */
    @Test
    public void getGamesOwnedByUsernameEmptyListTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        User user = insertTestingUser(username, password);

        List<GameOwned> result = gamesOwnedService.getGamesOwnedByUsername(user);

        var emptyResult = result.isEmpty();
        assertTrue("getGamesOwnedByUsernameEmptyListTest: result should be empty", emptyResult);
    }

    /**
     * Verifies that retrieving games with a null user returns an empty list.
     */
    @Test
    public void getGamesOwnedByUsernameNullUserTest() {
        List<GameOwned> result = gamesOwnedService.getGamesOwnedByUsername(null);

        assertNotNull("getGamesOwnedByUsernameNullUserTest: result should not be null", result);
        var emptyResult = result.isEmpty();
        assertTrue("getGamesOwnedByUsernameNullUserTest: result should be empty", emptyResult);
    }

    /*
     * = = = = = = = = = = =
     * Add Game to Collection Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that a user can add an existing game to their collection.
     */
    @Test
    public void addGameToCollectionSuccessTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        User user = insertTestingUser(username, password);
        Game game = insertTestingPublicGame("Owned Test Game");

        boolean result = gamesOwnedService.addGameToCollection(user, game.getGameID(), 2020);
        assertTrue("addGameToCollectionSuccessTest: should add the game successfully", result);
        List<GameOwned> ownedGames = gamesOwnedService.getGamesOwnedByUsername(user);
        var resultSize = ownedGames.size();
        assertEquals("addGameToCollectionSuccessTest: user should own one game", 1, resultSize);
        assertEquals("addGameToCollectionSuccessTest: should link the public game",
                game.getGameID(), ownedGames.getFirst().getGame().getGameID());
        assertEquals("addGameToCollectionSuccessTest: ownership should belong to the requesting user",
                user, ownedGames.getFirst().getUser());
        assertEquals("addGameToCollectionSuccessTest: production year should match",
                2020, ownedGames.getFirst().getYearProduced());
    }

    /**
     * Verifies that a game can be added when its production year is unknown.
     */
    @Test
    public void addGameToCollectionNullYearTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        User user = insertTestingUser(username, password);
        Game game = insertTestingGame(user, "Owned Test Game");

        boolean result = gamesOwnedService.addGameToCollection(user, game.getGameID(), null);
        assertTrue("addGameToCollectionNullYearTest: should allow a null production year", result);
        List<GameOwned> ownedGames = gamesOwnedService.getGamesOwnedByUsername(user);
        var resultSize = ownedGames.size();
        assertEquals("addGameToCollectionNullYearTest: user should own one game", 1, resultSize);
        assertNull("addGameToCollectionNullYearTest: production year should be null",
                ownedGames.getFirst().getYearProduced());
    }

    /**
     * Verifies that adding a game with a null user fails.
     */
    @Test
    public void addGameToCollectionNullUserTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        User user = insertTestingUser(username, password);
        Game game = insertTestingGame(user, "Owned Test Game");

        boolean result = gamesOwnedService.addGameToCollection(null, game.getGameID(), 2020);

        assertFalse("addGameToCollectionNullUserTest: should fail with a null user", result);
        assertTrue("addGameToCollectionNullUserTest: should leave the collection empty",
                gamesOwnedService.getGamesOwnedByUsername(user).isEmpty());
    }

    /**
     * Verifies that adding a game with a null game ID fails.
     */
    @Test
    public void addGameToCollectionNullGameIDTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        User user = insertTestingUser(username, password);

        boolean result = gamesOwnedService.addGameToCollection(user, null, 2020);

        assertFalse("addGameToCollectionNullGameIDTest: should fail with a null game ID", result);
    }

    /**
     * Verifies that adding a game that does not exist fails.
     */
    @Test
    public void addGameToCollectionNonexistentGameTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        User user = insertTestingUser(username, password);

        boolean result = gamesOwnedService.addGameToCollection(user, -1L, 2020);

        assertFalse("addGameToCollectionNonexistentGameTest: should fail with a nonexistent game", result);
    }

    /**
     * Verifies that production years below -2000 are rejected.
     */
    @Test
    public void addGameToCollectionYearTooEarlyTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        User user = insertTestingUser(username, password);
        Game game = insertTestingGame(user, "Owned Test Game");

        boolean result = gamesOwnedService.addGameToCollection(
                user, game.getGameID(), -2001);

        assertFalse("addGameToCollectionYearTooEarlyTest: should reject an early year",
                result);
        assertTrue("addGameToCollectionYearTooEarlyTest: should leave the collection empty",
                gamesOwnedService.getGamesOwnedByUsername(user).isEmpty());
    }

    /**
     * Verifies that production years more than two years ahead are rejected.
     */
    @Test
    public void addGameToCollectionYearTooLateTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        User user = insertTestingUser(username, password);
        Game game = insertTestingGame(user, "Owned Test Game");

        boolean result = gamesOwnedService.addGameToCollection(
                user, game.getGameID(), Year.now().getValue() + 3);

        assertFalse("addGameToCollectionYearTooLateTest: should reject a future year",
                result);
        assertTrue("addGameToCollectionYearTooLateTest: should leave the collection empty",
                gamesOwnedService.getGamesOwnedByUsername(user).isEmpty());
    }

    /**
     * Verifies that the earliest allowed production year is accepted.
     */
    @Test
    public void addGameToCollectionMinimumYearTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        User user = insertTestingUser(username, password);
        Game game = insertTestingGame(user, "Owned Test Game");

        boolean result = gamesOwnedService.addGameToCollection(
                user, game.getGameID(), -2000);

        assertTrue("addGameToCollectionMinimumYearTest: should accept -2000", result);
        assertEquals("addGameToCollectionMinimumYearTest: should save the year",
                Integer.valueOf(-2000),
                gamesOwnedService.getGamesOwnedByUsername(user)
                        .getFirst().getYearProduced());
    }

    /**
     * Verifies that a production year two years ahead is accepted.
     */
    @Test
    public void addGameToCollectionMaximumYearTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        User user = insertTestingUser(username, password);
        Game game = insertTestingGame(user, "Owned Test Game");
        int maximumYear = Year.now().getValue() + 2;

        boolean result = gamesOwnedService.addGameToCollection(
                user, game.getGameID(), maximumYear);

        assertTrue("addGameToCollectionMaximumYearTest: should accept the maximum year",
                result);
        assertEquals("addGameToCollectionMaximumYearTest: should save the year",
                Integer.valueOf(maximumYear),
                gamesOwnedService.getGamesOwnedByUsername(user)
                        .getFirst().getYearProduced());
    }

    /**
     * Verifies that a user can own multiple copies of the same game.
     */
    @Test
    public void addGameToCollectionMultipleCopiesTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        User user = insertTestingUser(username, password);
        Game game = insertTestingGame(user, "Owned Test Game");

        boolean firstResult = gamesOwnedService.addGameToCollection(
                user, game.getGameID(), 2020);
        boolean secondResult = gamesOwnedService.addGameToCollection(
                user, game.getGameID(), 2021);

        assertTrue("addGameToCollectionMultipleCopiesTest: should add the first copy",
                firstResult);
        assertTrue("addGameToCollectionMultipleCopiesTest: should add the second copy",
                secondResult);
        assertEquals("addGameToCollectionMultipleCopiesTest: should save two records",
                2, gamesOwnedService.getGamesOwnedByUsername(user).size());
    }

    /*
     * = = = = = = = = = = =
     * Remove Game from Collection Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that a user can remove a game from their collection.
     */
    @Test
    public void removeGameFromCollectionSuccessTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        User user = insertTestingUser(username, password);
        Game game = insertTestingGame(user, "Owned Test Game");
        insertTestingOwnedGame(user, game, 2020);
        List<GameOwned> ownedGames = gamesOwnedService.getGamesOwnedByUsername(user);
        var resultSize = ownedGames.size();
        assertEquals("removeGameFromCollectionSuccessTest: user should own one game", 1, resultSize);
        Long gameOwnedID = ownedGames.getFirst().getGameOwnedID();

        boolean result = gamesOwnedService.removeGameFromCollection(user, gameOwnedID);

        assertTrue("removeGameFromCollectionSuccessTest: should remove the game successfully", result);
        assertTrue("removeGameFromCollectionSuccessTest: user's collection should be empty",
                gamesOwnedService.getGamesOwnedByUsername(user).isEmpty());
    }

    /**
     * Verifies that removing a game with a null user fails.
     */
    @Test
    public void removeGameFromCollectionNullUserTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        // No owned games in the database
        boolean result = gamesOwnedService.removeGameFromCollection(null, -1L);
        assertFalse("removeGameFromCollectionNullUserTest: should fail with no owned games", result);

        // One owned game in the database
        User user = insertTestingUser(username, password);
        Game game = insertTestingGame(user, "Owned Test Game");
        GameOwned ownedGame = insertTestingOwnedGame(user, game, 2020);
        result = gamesOwnedService.removeGameFromCollection(null, ownedGame.getGameOwnedID());
        assertFalse("removeGameFromCollectionNullUserTest: should fail even when the owned game exists", result);
        assertEquals("removeGameFromCollectionNullUserTest: should preserve the user's copy",
                ownedGame, gamesOwnedService.getGameOwnedByID(user, ownedGame.getGameOwnedID()));
    }

    /**
     * Verifies that removing a game with a null ownership ID fails.
     */
    @Test
    public void removeGameFromCollectionNullIDTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        User user = insertTestingUser(username, password);

        boolean result = gamesOwnedService.removeGameFromCollection(user, null);

        assertFalse("removeGameFromCollectionNullIDTest: should fail with a null ownership ID", result);
    }

    /**
     * Verifies that removing a nonexistent ownership record fails.
     */
    @Test
    public void removeGameFromCollectionNonexistentIDTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        User user = insertTestingUser(username, password);

        boolean result = gamesOwnedService.removeGameFromCollection(user, -1L);

        assertFalse("removeGameFromCollectionNonexistentIDTest: should fail with a nonexistent ID", result);
    }

    /**
     * Verifies that a user cannot remove a game owned by another user.
     */
    @Test
    public void removeGameFromCollectionWrongUserTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        User user = insertTestingUser(username, password);
        User otherUser = insertTestingUser("ownedtestuser2", password);
        Game game = insertTestingGame(user, "Owned Test Game");
        insertTestingOwnedGame(user, game, 2020);
        List<GameOwned> ownedGames = gamesOwnedService.getGamesOwnedByUsername(user);
        var resultSize = ownedGames.size();
        assertEquals("removeGameFromCollectionWrongUserTest: user should own one game", 1, resultSize);
        Long gameOwnedID = ownedGames.getFirst().getGameOwnedID();

        boolean result = gamesOwnedService.removeGameFromCollection(otherUser, gameOwnedID);

        assertFalse("removeGameFromCollectionWrongUserTest: should fail when the game belongs to another user",
                result);
        assertEquals("removeGameFromCollectionWrongUserTest: original owner should still own the game",
                1, gamesOwnedService.getGamesOwnedByUsername(user).size());
    }

    /*
     * = = = = = = = = = = =
     * Get Game Owned By ID Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that a user can retrieve their own ownership record.
     */
    @Test
    public void getGameOwnedByIDSuccessTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        User user = insertTestingUser(username, password);
        Game game = insertTestingGame(user, "Owned Test Game");
        GameOwned ownedGame = insertTestingOwnedGame(user, game, 2020);

        GameOwned result = gamesOwnedService.getGameOwnedByID(
                user, ownedGame.getGameOwnedID());

        assertNotNull("getGameOwnedByIDSuccessTest: should find the ownership record",
                result);
        var actualGameOwnedID = result.getGameOwnedID();
        assertEquals("getGameOwnedByIDSuccessTest: should return the matching record",
                ownedGame.getGameOwnedID(), actualGameOwnedID);
        assertEquals("getGameOwnedByIDSuccessTest: should return the correct game",
                game.getGameID(), result.getGame().getGameID());
    }

    /**
     * Verifies that a null user cannot retrieve an ownership record.
     */
    @Test
    public void getGameOwnedByIDNullUserTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        User user = insertTestingUser(username, password);
        GameOwned ownedGame = insertTestingOwnedGame(
                user, insertTestingGame(user, "Owned Test Game"), 2020);

        GameOwned result = gamesOwnedService.getGameOwnedByID(
                null, ownedGame.getGameOwnedID());

        assertNull("getGameOwnedByIDNullUserTest: should reject a null user", result);
    }

    /**
     * Verifies that a null ownership ID returns null.
     */
    @Test
    public void getGameOwnedByIDNullIDTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        User user = insertTestingUser(username, password);

        GameOwned result = gamesOwnedService.getGameOwnedByID(user, null);

        assertNull("getGameOwnedByIDNullIDTest: should reject a null ID", result);
    }

    /**
     * Verifies that a nonexistent ownership ID returns null.
     */
    @Test
    public void getGameOwnedByIDNonexistentIDTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        User user = insertTestingUser(username, password);

        GameOwned result = gamesOwnedService.getGameOwnedByID(user, -1L);

        assertNull("getGameOwnedByIDNonexistentIDTest: should return null", result);
    }

    /**
     * Verifies that a user cannot retrieve another user's ownership record.
     */
    @Test
    public void getGameOwnedByIDWrongUserTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        User user = insertTestingUser(username, password);
        User otherUser = insertTestingUser("ownedtestuser2", password);
        GameOwned ownedGame = insertTestingOwnedGame(
                user, insertTestingGame(user, "Owned Test Game"), 2020);

        GameOwned result = gamesOwnedService.getGameOwnedByID(
                otherUser, ownedGame.getGameOwnedID());

        assertNull("getGameOwnedByIDWrongUserTest: should reject another user's record",
                result);
    }

    /*
     * = = = = = = = = = = =
     * Get Number Of References Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that reference counts include only the selected user's
     * copies of the selected game.
     */
    @Test
    public void getNumberOfReferencesSuccessTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        User user = insertTestingUser(username, password);
        User otherUser = insertTestingUser("ownedtestuser2", password);
        Game game = insertTestingGame(user, "Owned Test Game");
        Game otherGame = insertTestingGame(user, "Other Game");

        insertTestingOwnedGame(user, game, 2020);
        insertTestingOwnedGame(user, game, 2021);
        insertTestingOwnedGame(user, otherGame, 2020);
        insertTestingOwnedGame(otherUser, game, 2020);

        long result = gamesOwnedService.getNumberOfReferences(
                game.getGameID(), user);

        assertEquals("getNumberOfReferencesSuccessTest: should count only matching records",
                2L, result);
    }

    /**
     * Verifies that a game without ownership records has zero references.
     */
    @Test
    public void getNumberOfReferencesNoReferencesTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        User user = insertTestingUser(username, password);
        Game game = insertTestingGame(user, "Owned Test Game");

        long result = gamesOwnedService.getNumberOfReferences(
                game.getGameID(), user);

        assertEquals("getNumberOfReferencesNoReferencesTest: should return zero",
                0L, result);
    }

    /**
     * Verifies that a null user produces a zero reference count.
     */
    @Test
    public void getNumberOfReferencesNullUserTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        User user = insertTestingUser(username, password);
        Game game = insertTestingGame(user, "Owned Test Game");

        long result = gamesOwnedService.getNumberOfReferences(
                game.getGameID(), null);

        assertEquals("getNumberOfReferencesNullUserTest: should return zero",
                0L, result);
    }

    /**
     * Verifies that a null game ID produces a zero reference count.
     */
    @Test
    public void getNumberOfReferencesNullGameIDTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        User user = insertTestingUser(username, password);

        long result = gamesOwnedService.getNumberOfReferences(null, user);

        assertEquals("getNumberOfReferencesNullGameIDTest: should return zero",
                0L, result);
    }

    /**
     * Verifies that a nonexistent game has zero references.
     */
    @Test
    public void getNumberOfReferencesNonexistentGameTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        User user = insertTestingUser(username, password);

        long result = gamesOwnedService.getNumberOfReferences(-1L, user);

        assertEquals("getNumberOfReferencesNonexistentGameTest: should return zero",
                0L, result);
    }

    /*
     * = = = = = = = = = = =
     * Update Game References Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that all matching references are updated while unrelated
     * games, other users' records, and production years are preserved.
     */
    @Test
    public void updateGameReferencesSuccessTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        User user = insertTestingUser(username, password);
        User otherUser = insertTestingUser("ownedtestuser2", password);
        Game originalGame = insertTestingGame(user, "Owned Test Game");
        Game replacementGame = insertTestingGame(user, "Replacement Game");
        Game unrelatedGame = insertTestingGame(user, "Unrelated Game");

        GameOwned firstCopy = insertTestingOwnedGame(user, originalGame, 2020);
        GameOwned secondCopy = insertTestingOwnedGame(user, originalGame, 2021);
        GameOwned unrelatedCopy = insertTestingOwnedGame(user, unrelatedGame, 2019);
        GameOwned otherUserCopy = insertTestingOwnedGame(otherUser, originalGame, 2018);

        boolean result = gamesOwnedService.updateGameReferences(
                originalGame.getGameID(), user, replacementGame);

        assertTrue("updateGameReferencesSuccessTest: should update references", result);

        GameOwned updatedFirst = gamesOwnedService.getGameOwnedByID(
                user, firstCopy.getGameOwnedID());
        GameOwned updatedSecond = gamesOwnedService.getGameOwnedByID(
                user, secondCopy.getGameOwnedID());

        assertEquals("updateGameReferencesSuccessTest: should update the first copy",
                replacementGame.getGameID(), updatedFirst.getGame().getGameID());
        assertEquals("updateGameReferencesSuccessTest: should update the second copy",
                replacementGame.getGameID(), updatedSecond.getGame().getGameID());
        var actualYearProduced = updatedFirst.getYearProduced();
        assertEquals("updateGameReferencesSuccessTest: should preserve the first year",
                Integer.valueOf(2020), actualYearProduced);
        var actualYearProduced2 = updatedSecond.getYearProduced();
        assertEquals("updateGameReferencesSuccessTest: should preserve the second year",
                Integer.valueOf(2021), actualYearProduced2);
        assertEquals("updateGameReferencesSuccessTest: should preserve unrelated games",
                unrelatedGame.getGameID(),
                gamesOwnedService.getGameOwnedByID(user, unrelatedCopy.getGameOwnedID())
                        .getGame().getGameID());
        assertEquals("updateGameReferencesSuccessTest: should preserve another user's copy",
                originalGame.getGameID(),
                gamesOwnedService.getGameOwnedByID(otherUser, otherUserCopy.getGameOwnedID())
                        .getGame().getGameID());
    }

    /**
     * Verifies that updating references succeeds when there are no matches.
     */
    @Test
    public void updateGameReferencesNoReferencesTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        User user = insertTestingUser(username, password);
        Game originalGame = insertTestingGame(user, "Owned Test Game");
        Game replacementGame = insertTestingGame(user, "Replacement Game");

        boolean result = gamesOwnedService.updateGameReferences(
                originalGame.getGameID(), user, replacementGame);

        assertTrue("updateGameReferencesNoReferencesTest: should succeed with no matches",
                result);
        assertTrue("updateGameReferencesNoReferencesTest: should not create ownership records",
                gamesOwnedService.getGamesOwnedByUsername(user).isEmpty());
    }

    /**
     * Verifies that a null original game ID is rejected.
     */
    @Test
    public void updateGameReferencesNullGameIDTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        User user = insertTestingUser(username, password);
        Game game = insertTestingGame(user, "Owned Test Game");

        boolean result = gamesOwnedService.updateGameReferences(null, user, game);

        assertFalse("updateGameReferencesNullGameIDTest: should reject a null ID",
                result);
    }

    /**
     * Verifies that a null user is rejected.
     */
    @Test
    public void updateGameReferencesNullUserTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        User user = insertTestingUser(username, password);
        Game game = insertTestingGame(user, "Owned Test Game");

        boolean result = gamesOwnedService.updateGameReferences(
                game.getGameID(), null, game);

        assertFalse("updateGameReferencesNullUserTest: should reject a null user",
                result);
    }

    /**
     * Verifies that a null replacement game is rejected without changing
     * an existing reference.
     */
    @Test
    public void updateGameReferencesNullNewGameTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        User user = insertTestingUser(username, password);
        Game game = insertTestingGame(user, "Owned Test Game");
        GameOwned ownedGame = insertTestingOwnedGame(user, game, 2020);

        boolean result = gamesOwnedService.updateGameReferences(
                game.getGameID(), user, null);

        assertFalse("updateGameReferencesNullNewGameTest: should reject a null game",
                result);
        assertEquals("updateGameReferencesNullNewGameTest: should preserve the reference",
                game.getGameID(),
                gamesOwnedService.getGameOwnedByID(user, ownedGame.getGameOwnedID())
                        .getGame().getGameID());
    }

    /*
     * = = = = = = = = = = =
     * Edit Game In Collection Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that an ownership record's game and year can be updated.
     */
    @Test
    public void editGameInCollectionSuccessTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        User user = insertTestingUser(username, password);
        Game originalGame = insertTestingGame(user, "Owned Test Game");
        Game replacementGame = insertTestingGame(user, "Replacement Game");
        GameOwned ownedGame = insertTestingOwnedGame(user, originalGame, 2020);

        boolean result = gamesOwnedService.editGameInCollection(
                user, ownedGame.getGameOwnedID(), replacementGame, 2021);

        assertTrue("editGameInCollectionSuccessTest: should update the record", result);

        GameOwned updatedGame = gamesOwnedService.getGameOwnedByID(
                user, ownedGame.getGameOwnedID());

        assertEquals("editGameInCollectionSuccessTest: should update the game",
                replacementGame.getGameID(), updatedGame.getGame().getGameID());
        var actualYearProduced = updatedGame.getYearProduced();
        assertEquals("editGameInCollectionSuccessTest: should update the year",
                Integer.valueOf(2021), actualYearProduced);
        var actualUser = updatedGame.getUser();
        assertEquals("editGameInCollectionSuccessTest: should preserve the owner",
                user, actualUser);
        assertEquals("editGameInCollectionSuccessTest: should preserve the record count",
                1, gamesOwnedService.getGamesOwnedByUsername(user).size());
    }

    /**
     * Verifies that an existing production year can be cleared.
     */
    @Test
    public void editGameInCollectionNullYearTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        User user = insertTestingUser(username, password);
        Game game = insertTestingGame(user, "Owned Test Game");
        GameOwned ownedGame = insertTestingOwnedGame(user, game, 2020);

        boolean result = gamesOwnedService.editGameInCollection(
                user, ownedGame.getGameOwnedID(), game, null);

        assertTrue("editGameInCollectionNullYearTest: should allow a null year", result);
        assertNull("editGameInCollectionNullYearTest: should clear the year",
                gamesOwnedService.getGameOwnedByID(user, ownedGame.getGameOwnedID())
                        .getYearProduced());
    }

    /**
     * Verifies that editing with a null user fails.
     */
    @Test
    public void editGameInCollectionNullUserTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        User user = insertTestingUser(username, password);
        Game game = insertTestingGame(user, "Owned Test Game");
        GameOwned ownedGame = insertTestingOwnedGame(user, game, 2020);

        boolean result = gamesOwnedService.editGameInCollection(
                null, ownedGame.getGameOwnedID(), game, 2021);

        assertFalse("editGameInCollectionNullUserTest: should reject a null user",
                result);
    }

    /**
     * Verifies that editing with a null ownership ID fails.
     */
    @Test
    public void editGameInCollectionNullIDTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        User user = insertTestingUser(username, password);
        Game game = insertTestingGame(user, "Owned Test Game");

        boolean result = gamesOwnedService.editGameInCollection(
                user, null, game, 2021);

        assertFalse("editGameInCollectionNullIDTest: should reject a null ID",
                result);
    }

    /**
     * Verifies that editing with a null game fails.
     */
    @Test
    public void editGameInCollectionNullGameTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        User user = insertTestingUser(username, password);
        Game game = insertTestingGame(user, "Owned Test Game");
        GameOwned ownedGame = insertTestingOwnedGame(user, game, 2020);

        boolean result = gamesOwnedService.editGameInCollection(
                user, ownedGame.getGameOwnedID(), null, 2021);

        assertFalse("editGameInCollectionNullGameTest: should reject a null game",
                result);
        var actualYearProduced = ownedGame.getYearProduced();
        assertEquals("editGameInCollectionNullGameTest: should preserve the year",
                Integer.valueOf(2020), actualYearProduced);
    }

    /**
     * Verifies that editing a nonexistent ownership record fails.
     */
    @Test
    public void editGameInCollectionNonexistentIDTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        User user = insertTestingUser(username, password);
        Game game = insertTestingGame(user, "Owned Test Game");

        boolean result = gamesOwnedService.editGameInCollection(
                user, -1L, game, 2021);

        assertFalse("editGameInCollectionNonexistentIDTest: should reject a nonexistent ID",
                result);
    }

    /**
     * Verifies that a user cannot edit another user's ownership record.
     */
    @Test
    public void editGameInCollectionWrongUserTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        User user = insertTestingUser(username, password);
        User otherUser = insertTestingUser("ownedtestuser2", password);
        Game originalGame = insertTestingGame(user, "Owned Test Game");
        Game replacementGame = insertTestingGame(user, "Replacement Game");
        GameOwned ownedGame = insertTestingOwnedGame(user, originalGame, 2020);

        boolean result = gamesOwnedService.editGameInCollection(
                otherUser, ownedGame.getGameOwnedID(), replacementGame, 2021);

        assertFalse("editGameInCollectionWrongUserTest: should reject another user's record",
                result);

        GameOwned unchangedGame = gamesOwnedService.getGameOwnedByID(
                user, ownedGame.getGameOwnedID());

        assertEquals("editGameInCollectionWrongUserTest: should preserve the game",
                originalGame.getGameID(), unchangedGame.getGame().getGameID());
        var actualYearProduced = unchangedGame.getYearProduced();
        assertEquals("editGameInCollectionWrongUserTest: should preserve the year",
                Integer.valueOf(2020), actualYearProduced);
    }

    /**
     * Verifies that an early year is rejected before either field is changed.
     */
    @Test
    public void editGameInCollectionYearTooEarlyTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        User user = insertTestingUser(username, password);
        Game originalGame = insertTestingGame(user, "Owned Test Game");
        Game replacementGame = insertTestingGame(user, "Replacement Game");
        GameOwned ownedGame = insertTestingOwnedGame(user, originalGame, 2020);

        boolean result = gamesOwnedService.editGameInCollection(
                user, ownedGame.getGameOwnedID(), replacementGame, -2001);

        assertFalse("editGameInCollectionYearTooEarlyTest: should reject an early year",
                result);

        GameOwned unchangedGame = gamesOwnedService.getGameOwnedByID(
                user, ownedGame.getGameOwnedID());

        assertEquals("editGameInCollectionYearTooEarlyTest: should preserve the game",
                originalGame.getGameID(), unchangedGame.getGame().getGameID());
        var actualYearProduced = unchangedGame.getYearProduced();
        assertEquals("editGameInCollectionYearTooEarlyTest: should preserve the year",
                Integer.valueOf(2020), actualYearProduced);
    }

    /**
     * Verifies that a future year is rejected before either field is changed.
     */
    @Test
    public void editGameInCollectionYearTooLateTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        User user = insertTestingUser(username, password);
        Game originalGame = insertTestingGame(user, "Owned Test Game");
        Game replacementGame = insertTestingGame(user, "Replacement Game");
        GameOwned ownedGame = insertTestingOwnedGame(user, originalGame, 2020);

        boolean result = gamesOwnedService.editGameInCollection(
                user, ownedGame.getGameOwnedID(), replacementGame,
                Year.now().getValue() + 3);

        assertFalse("editGameInCollectionYearTooLateTest: should reject a future year",
                result);

        GameOwned unchangedGame = gamesOwnedService.getGameOwnedByID(
                user, ownedGame.getGameOwnedID());

        assertEquals("editGameInCollectionYearTooLateTest: should preserve the game",
                originalGame.getGameID(), unchangedGame.getGame().getGameID());
        var actualYearProduced = unchangedGame.getYearProduced();
        assertEquals("editGameInCollectionYearTooLateTest: should preserve the year",
                Integer.valueOf(2020), actualYearProduced);
    }

    /**
     * Verifies that editing allows the earliest valid production year.
     */
    @Test
    public void editGameInCollectionMinimumYearTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        User user = insertTestingUser(username, password);
        Game game = insertTestingGame(user, "Owned Test Game");
        GameOwned ownedGame = insertTestingOwnedGame(user, game, 2020);

        boolean result = gamesOwnedService.editGameInCollection(
                user, ownedGame.getGameOwnedID(), game, -2000);

        assertTrue("editGameInCollectionMinimumYearTest: should accept -2000", result);
        assertEquals("editGameInCollectionMinimumYearTest: should save the year",
                Integer.valueOf(-2000),
                gamesOwnedService.getGameOwnedByID(user, ownedGame.getGameOwnedID())
                        .getYearProduced());
    }

    /**
     * Verifies that editing allows a year two years ahead.
     */
    @Test
    public void editGameInCollectionMaximumYearTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        User user = insertTestingUser(username, password);
        Game game = insertTestingGame(user, "Owned Test Game");
        GameOwned ownedGame = insertTestingOwnedGame(user, game, 2020);
        int maximumYear = Year.now().getValue() + 2;

        boolean result = gamesOwnedService.editGameInCollection(
                user, ownedGame.getGameOwnedID(), game, maximumYear);

        assertTrue("editGameInCollectionMaximumYearTest: should accept the maximum year",
                result);
        assertEquals("editGameInCollectionMaximumYearTest: should save the year",
                Integer.valueOf(maximumYear),
                gamesOwnedService.getGameOwnedByID(user, ownedGame.getGameOwnedID())
                        .getYearProduced());
    }

    /*
     * = = = = = = = = = = =
     * Get Games Owned By Family Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that a family without memberships has no owned games.
     */
    @Test
    public void getGamesOwnedByFamilyNoMembersTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        User user = insertTestingUser(username, password);
        Family family = insertTestingFamilyWithoutMembers(user);
        insertTestingOwnedGame(user, insertTestingGame(user, "Owned Test Game"), 2020);

        List<GameOwned> result = gamesOwnedService.getGamesOwnedByFamily(family);

        assertNotNull("getGamesOwnedByFamilyNoMembersTest: should return a list",
                result);
        var emptyResult = result.isEmpty();
        assertTrue("getGamesOwnedByFamilyNoMembersTest: should return an empty list",
                emptyResult);
    }

    /**
     * Verifies that only accepted members of the selected family contribute
     * games, including multiple copies of the same game.
     */
    @Test
    public void getGamesOwnedByFamilyAcceptedMembersTest() {
        final String username = "ownedtestuser";
        final String password = "ownedtestpass";

        User user = insertTestingUser(username, password);
        User secondMember = insertTestingUser("ownedtestuser2", password);
        User pendingMember = insertTestingUser("ownedtestuser3", password);
        User outsider = insertTestingUser("ownedtestuser4", password);

        Family family = insertTestingFamily(user, "Test Family");
        insertTestingFamily(outsider, "Other Family");
        FamilyMembership memberRequest = insertTestingPendingRequest(family, secondMember);
        boolean accepted = membershipService.acceptRequest(user, memberRequest);
        assertTrue("getGamesOwnedByFamilyAcceptedMembersTest: should accept the second member", accepted);
        assertEquals("getGamesOwnedByFamilyAcceptedMembersTest: new member should belong to the first family",
                family, membershipService.getFamilyForUser(secondMember));
        insertTestingPendingRequest(family, pendingMember);

        User rejectedMember = insertTestingUser("rejectedcollector", password);
        FamilyMembership rejectedRequest = insertTestingPendingRequest(family, rejectedMember);
        boolean rejected = membershipService.rejectRequest(user, rejectedRequest);
        assertTrue("getGamesOwnedByFamilyAcceptedMembersTest: should reject the other applicant", rejected);

        Game game = insertTestingGame(user, "Owned Test Game");
        GameOwned firstCopy = insertTestingOwnedGame(user, game, 2020);
        GameOwned secondCopy = insertTestingOwnedGame(user, game, 2021);
        GameOwned memberCopy = insertTestingOwnedGame(secondMember, game, 2022);
        GameOwned pendingCopy = insertTestingOwnedGame(pendingMember, game, 2020);
        GameOwned outsiderCopy = insertTestingOwnedGame(outsider, game, 2020);
        GameOwned rejectedCopy = insertTestingOwnedGame(rejectedMember, game, 2020);

        List<GameOwned> result = gamesOwnedService.getGamesOwnedByFamily(family);

        var resultSize = result.size();
        assertEquals("getGamesOwnedByFamilyAcceptedMembersTest: should return three records",
                3, resultSize);
        var containsExpectedItem = result.contains(firstCopy);
        assertTrue("getGamesOwnedByFamilyAcceptedMembersTest: should include the first copy",
                containsExpectedItem);
        var containsExpectedItem2 = result.contains(secondCopy);
        assertTrue("getGamesOwnedByFamilyAcceptedMembersTest: should include the second copy",
                containsExpectedItem2);
        var containsExpectedItem3 = result.contains(memberCopy);
        assertTrue("getGamesOwnedByFamilyAcceptedMembersTest: should include the other accepted member",
                containsExpectedItem3);
        var containsExpectedItem4 = result.contains(pendingCopy);
        assertFalse("getGamesOwnedByFamilyAcceptedMembersTest: should exclude pending members",
                containsExpectedItem4);
        var containsExpectedItem5 = result.contains(outsiderCopy);
        assertFalse("getGamesOwnedByFamilyAcceptedMembersTest: should exclude another family's member",
                containsExpectedItem5);
        assertFalse("getGamesOwnedByFamilyAcceptedMembersTest: should exclude rejected applicants",
                result.contains(rejectedCopy));
    }

    /**
     * Verifies that retrieving games for a null family returns an empty list.
     */
    @Test
    public void getGamesOwnedByFamilyNullFamilyTest() {
        List<GameOwned> result = gamesOwnedService.getGamesOwnedByFamily(null);

        assertNotNull("getGamesOwnedByFamilyNullFamilyTest: result should not be null", result);
        var emptyResult = result.isEmpty();
        assertTrue("getGamesOwnedByFamilyNullFamilyTest: result should be empty", emptyResult);
    }

    /*
     * = = = = = = = = = = =
     * Test Helpers
     * = = = = = = = = = = =
     */

    /**
     * Creates a test user and verifies that the saved credentials work.
     *
     * @param username the username of the user to create
     * @param password the password of the user to create
     * @return the created user
     */
    private User insertTestingUser(String username, String password) {
        // Check whether the supplied credentials already authenticate.
        User user = userService.validateUser(username, password);
        assertNull("insertTestingUser: user should not exist yet", user);

        // Create the user and verify that it was saved with the expected details.
        user = userService.newUser(username, password);
        assertNotNull("insertTestingUser: user should exist after creation", user);
        assertNotNull("insertTestingUser: user should have an ID", user.getId());
        assertEquals("insertTestingUser: user should have the correct username", username, user.getUsername());

        // Verify that the newly created user's credentials successfully authenticate.
        User userCheck = userService.validateUser(username, password);
        assertNotNull("insertTestingUser: user should exist after validation", userCheck);
        assertEquals("insertTestingUser: login should return the created user's ID", user.getId(), userCheck.getId());
        assertEquals("insertTestingUser: login should return the expected username", username, userCheck.getUsername());
        return userCheck;
    }

    /**
     * Adds a catalog game and verifies that the owner can retrieve it.
     *
     * @param user the user who owns the game
     * @param gameName the name of the game to create
     * @return the created game
     */
    private Game insertTestingGame(User user, String gameName) {
        // Add the game to the catalog with a minimum of 1 and maximum of 4 players.
        boolean result = gamesService.addGame(user, gameName, 1, 4);
        assertTrue("insertTestingGame: should add the catalog game", result);

        // Find the newly added game in the list of games available to this user.
        List<Game> games = gamesService.getAvailableGames(user).stream()
                .filter(game -> gameName.equals(game.getGameName()))
                .toList();
        assertEquals("insertTestingGame: should find one game with the expected name", 1, games.size());
        Game game = games.getFirst();

        // Verify that the game has an ID and is associated with the user who added it.
        assertNotNull("insertTestingGame: game should have an ID", game.getGameID());
        assertEquals("insertTestingGame: should save the user who added the game", user, game.getUserWhoAdded());

        // Retrieve the game again using its ID to verify that it was saved correctly.
        Game saved = gamesService.getGameByGameID(game.getGameID());
        assertNotNull("insertTestingGame: should retrieve the game by its ID", saved);
        assertEquals("insertTestingGame: catalog lookup should return the created game ID", game.getGameID(), saved.getGameID());
        assertEquals("insertTestingGame: should save the minimum player count", 1, saved.getMinPlayers());
        assertEquals("insertTestingGame: should save the maximum player count", 4, saved.getMaxPlayers());
        assertTrue("insertTestingGame: should save a visible game", saved.getCanSee());
        return saved;
    }

    /**
     * Persists a public catalog game for tests that exercise shared ownership.
     *
     * @param gameName the name of the game to create
     * @return the created game
     */
    private Game insertTestingPublicGame(String gameName) {
        // Create a game without assigning a user as its owner.
        Game game = new Game(gameName);
        game.setCanSee(true);

        // Persist the public game directly and flush it to the database.
        entityManager.persist(game);
        entityManager.flush();

        // Verify that the public game was saved and can be retrieved by its ID.
        assertNotNull("insertTestingPublicGame: public game should have an ID", game.getGameID());
        Game saved = gamesService.getGameByGameID(game.getGameID());
        assertNotNull("insertTestingPublicGame: should retrieve the game by its ID", saved);
        assertEquals("insertTestingPublicGame: should save the expected name", gameName, saved.getGameName());
        assertNull("insertTestingPublicGame: should have no user owner", saved.getUserWhoAdded());
        assertTrue("insertTestingPublicGame: should save a visible game", saved.getCanSee());
        return saved;
    }

    /**
     * Adds one ownership record and validates the generated record and its details.
     *
     * @param user the user who owns the game
     * @param game the game being owned
     * @param yearProduced the year the game was produced
     * @return the created ownership record
     */
    private GameOwned insertTestingOwnedGame(User user, Game game, Integer yearProduced) {
        // Store the IDs of the games already in the user's collection.
        List<Long> previousIDs = gamesOwnedService.getGamesOwnedByUsername(user).stream()
                .map(GameOwned::getGameOwnedID)
                .toList();

        // Add the game to the user's collection with the specified production year.
        boolean result = gamesOwnedService.addGameToCollection(user, game.getGameID(), yearProduced);
        assertTrue("insertTestingOwnedGame: should add the test ownership record", result);

        // Retrieve the updated collection and verify that exactly one record was added.
        List<GameOwned> ownedGames = gamesOwnedService.getGamesOwnedByUsername(user);
        assertEquals("insertTestingOwnedGame: collection should grow by one", previousIDs.size() + 1, ownedGames.size());

        // Identify the new ownership record by finding an ID that was not in the original collection.
        List<GameOwned> newGames = ownedGames.stream()
                .filter(ownedGame -> !previousIDs.contains(ownedGame.getGameOwnedID()))
                .toList();
        assertEquals("insertTestingOwnedGame: should find exactly one new copy", 1, newGames.size());
        Long ownedID = newGames.getFirst().getGameOwnedID();
        assertNotNull("insertTestingOwnedGame: copy should have an ID", ownedID);

        // Retrieve the new ownership record by its ID and verify its saved information.
        GameOwned ownedGame = gamesOwnedService.getGameOwnedByID(user, ownedID);
        assertNotNull("insertTestingOwnedGame: should find the copy by its ID", ownedGame);
        assertEquals("insertTestingOwnedGame: should return the expected ownership ID", ownedID, ownedGame.getGameOwnedID());
        assertEquals("insertTestingOwnedGame: should save the owner", user, ownedGame.getUser());
        assertEquals("insertTestingOwnedGame: should save the game ID", game.getGameID(), ownedGame.getGame().getGameID());
        assertEquals("insertTestingOwnedGame: should save the production year", yearProduced, ownedGame.getYearProduced());
        return ownedGame;
    }

    /**
     * Creates a family and checks its administrator and initial membership.
     *
     * @param admin the administrator of the family
     * @param familyName the name of the family
     * @return the created family
     */
    private Family insertTestingFamily(User admin, String familyName) {
        // Verify that the administrator does not already belong to a family.
        assertNull("insertTestingFamily: the administrator should start without a family",
                membershipService.getFamilyForUser(admin));

        // Create the family with the specified administrator and name.
        boolean result = familyService.createFamily(admin, familyName);
        assertTrue("insertTestingFamily: should create the family", result);

        // Retrieve the family and verify its ID, name, and administrator.
        Family family = membershipService.getFamilyForUser(admin);
        assertNotNull("insertTestingFamily: should find the created family", family);
        assertNotNull("insertTestingFamily: family should have an ID", family.getFamilyID());
        assertEquals("insertTestingFamily: should store the expected name", familyName, family.getFamilyName());
        assertEquals("insertTestingFamily: should assign the administrator", admin, family.getFamilyAdmin());

        // Verify that creating the family also created an accepted membership for the administrator.
        List<FamilyMembership> memberships = membershipService.getFamilyMembers(admin);
        assertEquals("insertTestingFamily: should create exactly one membership", 1, memberships.size());
        assertEquals("insertTestingFamily: should accept the administrator", "ACCEPTED", memberships.getFirst().getStatus());
        assertEquals("insertTestingFamily: membership should belong to the administrator", admin, memberships.getFirst().getUser());
        return family;
    }

    /**
     * Submits a join request and verifies its persisted status and associations.
     *
     * @param family the family to request to join
     * @param applicant the user requesting to join the family
     * @return the created pending membership request
     */
    private FamilyMembership insertTestingPendingRequest(Family family, User applicant) {
        // Retrieve the administrator so the pending requests can be checked through their account.
        User admin = family.getFamilyAdmin();

        // Verify that the applicant does not already have a pending request for this family.
        assertTrue("insertTestingPendingRequest: applicant should have no pending request for this family",
                membershipService.getPendingRequests(admin).stream()
                        .noneMatch(request -> applicant.equals(request.getUser())));

        // Submit a request for the applicant to join the specified family.
        boolean result = membershipService.requestToJoinFamily(applicant, family.getFamilyID());
        assertTrue("insertTestingPendingRequest: should submit the request", result);

        // Find the applicant's request in the administrator's pending requests.
        List<FamilyMembership> requests = membershipService.getPendingRequests(admin).stream()
                .filter(request -> applicant.equals(request.getUser()))
                .toList();
        assertEquals("insertTestingPendingRequest: should find one request for the applicant", 1, requests.size());
        FamilyMembership request = requests.getFirst();

        // Verify that the request was saved with the correct ID, family, applicant, and status.
        assertNotNull("insertTestingPendingRequest: request should have an ID", request.getMembershipID());
        assertEquals("insertTestingPendingRequest: should save the expected family", family, request.getFamily());
        assertEquals("insertTestingPendingRequest: should save the expected applicant", applicant, request.getUser());
        assertEquals("insertTestingPendingRequest: should save pending status", "PENDING", request.getStatus());
        return request;
    }

    /**
     * Creates a family without the initial membership normally created by the service.
     *
     * @param admin the admin user for the family
     * @return the created family
     */
    private Family insertTestingFamilyWithoutMembers(User admin) {
        // Create and persist a family directly instead of using the service method.
        // This allows the test to create a family without its usual initial membership.
        Family family = new Family("Test Family", admin);
        entityManager.persist(family);
        entityManager.flush();

        // Verify that the family was saved and that no membership records were created for it.
        assertNotNull("insertTestingFamilyWithoutMembers: family should have an ID", family.getFamilyID());
        Long count = entityManager.createQuery("""
                    select count(membership) from FamilyMembership membership
                    where membership.family = :family
                    """, Long.class)
                .setParameter("family", family)
                .getSingleResult();
        assertEquals("insertTestingFamilyWithoutMembers: family should have no memberships", 0L, count);
        return family;
    }
}
