package edu.carroll.cs341.service;

import edu.carroll.cs341.jpa.model.Family;
import edu.carroll.cs341.jpa.model.FamilyMembership;
import edu.carroll.cs341.jpa.model.Game;
import edu.carroll.cs341.jpa.model.GameOwned;
import edu.carroll.cs341.jpa.model.User;
import edu.carroll.cs341.jpa.repo.FamiliesRepository;
import edu.carroll.cs341.jpa.repo.FamilyMembershipRepository;
import edu.carroll.cs341.jpa.repo.GamesRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Year;
import java.util.List;

import static org.springframework.test.util.AssertionErrors.*;

/**
 * Tests the {@link GamesOwnedService} methods responsible for managing users'
 * game collections and retrieving owned games.
 *
 * <p>Each test creates only the user and game fixtures it needs, then verifies
 * the expected results for valid and invalid inputs, including null values,
 * nonexistent games, invalid ownership IDs, and attempts to remove games
 * belonging to another user.</p>
 *
 * <p>The {@link SpringBootTest} annotation loads the Spring application
 * context so that the service and repository can be tested using their
 * configured dependencies. The {@link Transactional} annotation allows
 * database operations performed during each test to participate in a
 * transaction that is normally rolled back after the test completes.</p>
 *
 * <p>ChatGPT assisted with Javadoc documentation; code reviewed by author.
 * No code generated.</p>
 *
 * @author Max Montowski
 */
@SpringBootTest
@Transactional
public class GamesOwnedServiceTest {
    private static final String username = "ownedtestuser";
    private static final String password = "ownedtestpass";

    @Autowired
    private GamesOwnedService gamesOwnedService;

    @Autowired
    private UserService userService;

    @Autowired
    private GamesRepository gamesRepo;

    @Autowired
    private FamiliesRepository familiesRepo;

    @Autowired
    private FamilyMembershipRepository membershipRepo;
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
        User user = insertTestingUser();
        Game game = insertTestingGame();
        gamesOwnedService.addGameToCollection(user, game.getGameID(), null);

        List<GameOwned> result = gamesOwnedService.getGamesOwnedByUsername(user);

        assertNotNull("getGamesOwnedByUsernameReturnsListTest: result should not be null", result);
        assertEquals("getGamesOwnedByUsernameReturnsListTest: result should contain one game", 1, result.size());
        assertEquals("getGamesOwnedByUsernameReturnsListTest: result should contain the user's game",
                game, result.getFirst().getGame());
    }

    /**
     * Verifies that retrieving games for a user without owned games
     * returns an empty list.
     */
    @Test
    public void getGamesOwnedByUsernameEmptyListTest() {
        User user = insertTestingUser();

        List<GameOwned> result = gamesOwnedService.getGamesOwnedByUsername(user);

        assertTrue("getGamesOwnedByUsernameEmptyListTest: result should be empty", result.isEmpty());
    }

    /**
     * Verifies that retrieving games with a null user returns an empty list.
     */
    @Test
    public void getGamesOwnedByUsernameNullUserTest() {
        List<GameOwned> result = gamesOwnedService.getGamesOwnedByUsername(null);

        assertNotNull("getGamesOwnedByUsernameNullUserTest: result should not be null", result);
        assertTrue("getGamesOwnedByUsernameNullUserTest: result should be empty", result.isEmpty());
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
        User user = insertTestingUser();
        Game game = insertTestingGame();

        boolean result = gamesOwnedService.addGameToCollection(user, game.getGameID(), 2020);
        assertTrue("addGameToCollectionSuccessTest: should add the game successfully", result);
        List<GameOwned> ownedGames = gamesOwnedService.getGamesOwnedByUsername(user);
        assertEquals("addGameToCollectionSuccessTest: user should own one game", 1, ownedGames.size());
        assertEquals("addGameToCollectionSuccessTest: production year should match",
                2020, ownedGames.getFirst().getYearProduced());
    }

    /**
     * Verifies that a game can be added when its production year is unknown.
     */
    @Test
    public void addGameToCollectionNullYearTest() {
        User user = insertTestingUser();
        Game game = insertTestingGame();

        boolean result = gamesOwnedService.addGameToCollection(user, game.getGameID(), null);
        assertTrue("addGameToCollectionNullYearTest: should allow a null production year", result);
        List<GameOwned> ownedGames = gamesOwnedService.getGamesOwnedByUsername(user);
        assertEquals("addGameToCollectionNullYearTest: user should own one game", 1, ownedGames.size());
        assertNull("addGameToCollectionNullYearTest: production year should be null",
                ownedGames.getFirst().getYearProduced());
    }

    /**
     * Verifies that adding a game with a null user fails.
     */
    @Test
    public void addGameToCollectionNullUserTest() {
        Game game = insertTestingGame();

        boolean result = gamesOwnedService.addGameToCollection(null, game.getGameID(), 2020);

        assertFalse("addGameToCollectionNullUserTest: should fail with a null user", result);
    }

    /**
     * Verifies that adding a game with a null game ID fails.
     */
    @Test
    public void addGameToCollectionNullGameIDTest() {
        User user = insertTestingUser();

        boolean result = gamesOwnedService.addGameToCollection(user, null, 2020);

        assertFalse("addGameToCollectionNullGameIDTest: should fail with a null game ID", result);
    }

    /**
     * Verifies that adding a game that does not exist fails.
     */
    @Test
    public void addGameToCollectionNonexistentGameTest() {
        User user = insertTestingUser();

        boolean result = gamesOwnedService.addGameToCollection(user, -1L, 2020);

        assertFalse("addGameToCollectionNonexistentGameTest: should fail with a nonexistent game", result);
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
        User user = insertTestingUser();
        Game game = insertTestingGame();
        gamesOwnedService.addGameToCollection(user, game.getGameID(), 2020);
        List<GameOwned> ownedGames = gamesOwnedService.getGamesOwnedByUsername(user);
        assertEquals("removeGameFromCollectionSuccessTest: user should own one game", 1, ownedGames.size());
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
        boolean result = gamesOwnedService.removeGameFromCollection(null, 1L);

        assertFalse("removeGameFromCollectionNullUserTest: should fail with a null user", result);
    }

    /**
     * Verifies that removing a game with a null ownership ID fails.
     */
    @Test
    public void removeGameFromCollectionNullIDTest() {
        User user = insertTestingUser();

        boolean result = gamesOwnedService.removeGameFromCollection(user, null);

        assertFalse("removeGameFromCollectionNullIDTest: should fail with a null ownership ID", result);
    }

    /**
     * Verifies that removing a nonexistent ownership record fails.
     */
    @Test
    public void removeGameFromCollectionNonexistentIDTest() {
        User user = insertTestingUser();

        boolean result = gamesOwnedService.removeGameFromCollection(user, -1L);

        assertFalse("removeGameFromCollectionNonexistentIDTest: should fail with a nonexistent ID", result);
    }

    /**
     * Verifies that a user cannot remove a game owned by another user.
     */
    @Test
    public void removeGameFromCollectionWrongUserTest() {
        User user = insertTestingUser();
        User otherUser = userService.newUser("ownedtestuser2", password, password);
        Game game = insertTestingGame();
        gamesOwnedService.addGameToCollection(user, game.getGameID(), 2020);
        List<GameOwned> ownedGames = gamesOwnedService.getGamesOwnedByUsername(user);
        assertEquals("removeGameFromCollectionWrongUserTest: user should own one game", 1, ownedGames.size());
        Long gameOwnedID = ownedGames.getFirst().getGameOwnedID();

        boolean result = gamesOwnedService.removeGameFromCollection(otherUser, gameOwnedID);

        assertFalse("removeGameFromCollectionWrongUserTest: should fail when the game belongs to another user",
                result);
        assertEquals("removeGameFromCollectionWrongUserTest: original owner should still own the game",
                1, gamesOwnedService.getGamesOwnedByUsername(user).size());
    }

    /*
     * = = = = = = = = = = =
     * Additional Add Game Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that production years below -2000 are rejected.
     */
    @Test
    public void addGameToCollectionYearTooEarlyTest() {
        User user = insertTestingUser();
        Game game = insertTestingGame();

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
        User user = insertTestingUser();
        Game game = insertTestingGame();

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
        User user = insertTestingUser();
        Game game = insertTestingGame();

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
        User user = insertTestingUser();
        Game game = insertTestingGame();
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
        User user = insertTestingUser();
        Game game = insertTestingGame();

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
     * Get Game Owned By ID Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that a user can retrieve their own ownership record.
     */
    @Test
    public void getGameOwnedByIDSuccessTest() {
        User user = insertTestingUser();
        Game game = insertTestingGame();
        GameOwned ownedGame = insertTestingOwnedGame(user, game, 2020);

        GameOwned result = gamesOwnedService.getGameOwnedByID(
                user, ownedGame.getGameOwnedID());

        assertNotNull("getGameOwnedByIDSuccessTest: should find the ownership record",
                result);
        assertEquals("getGameOwnedByIDSuccessTest: should return the matching record",
                ownedGame.getGameOwnedID(), result.getGameOwnedID());
        assertEquals("getGameOwnedByIDSuccessTest: should return the correct game",
                game.getGameID(), result.getGame().getGameID());
    }

    /**
     * Verifies that a null user cannot retrieve an ownership record.
     */
    @Test
    public void getGameOwnedByIDNullUserTest() {
        User user = insertTestingUser();
        GameOwned ownedGame = insertTestingOwnedGame(
                user, insertTestingGame(), 2020);

        GameOwned result = gamesOwnedService.getGameOwnedByID(
                null, ownedGame.getGameOwnedID());

        assertNull("getGameOwnedByIDNullUserTest: should reject a null user", result);
    }

    /**
     * Verifies that a null ownership ID returns null.
     */
    @Test
    public void getGameOwnedByIDNullIDTest() {
        User user = insertTestingUser();

        GameOwned result = gamesOwnedService.getGameOwnedByID(user, null);

        assertNull("getGameOwnedByIDNullIDTest: should reject a null ID", result);
    }

    /**
     * Verifies that a nonexistent ownership ID returns null.
     */
    @Test
    public void getGameOwnedByIDNonexistentIDTest() {
        User user = insertTestingUser();

        GameOwned result = gamesOwnedService.getGameOwnedByID(user, -1L);

        assertNull("getGameOwnedByIDNonexistentIDTest: should return null", result);
    }

    /**
     * Verifies that a user cannot retrieve another user's ownership record.
     */
    @Test
    public void getGameOwnedByIDWrongUserTest() {
        User user = insertTestingUser();
        User otherUser = userService.newUser(
                "ownedtestuser2", password, password);
        GameOwned ownedGame = insertTestingOwnedGame(
                user, insertTestingGame(), 2020);

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
        User user = insertTestingUser();
        User otherUser = userService.newUser(
                "ownedtestuser2", password, password);
        Game game = insertTestingGame();
        Game otherGame = gamesRepo.save(new Game("Other Game"));

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
        User user = insertTestingUser();
        Game game = insertTestingGame();

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
        Game game = insertTestingGame();

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
        User user = insertTestingUser();

        long result = gamesOwnedService.getNumberOfReferences(null, user);

        assertEquals("getNumberOfReferencesNullGameIDTest: should return zero",
                0L, result);
    }

    /**
     * Verifies that a nonexistent game has zero references.
     */
    @Test
    public void getNumberOfReferencesNonexistentGameTest() {
        User user = insertTestingUser();

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
        User user = insertTestingUser();
        User otherUser = userService.newUser(
                "ownedtestuser2", password, password);
        Game originalGame = insertTestingGame();
        Game replacementGame = gamesRepo.save(new Game("Replacement Game"));
        Game unrelatedGame = gamesRepo.save(new Game("Unrelated Game"));

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
        assertEquals("updateGameReferencesSuccessTest: should preserve the first year",
                Integer.valueOf(2020), updatedFirst.getYearProduced());
        assertEquals("updateGameReferencesSuccessTest: should preserve the second year",
                Integer.valueOf(2021), updatedSecond.getYearProduced());
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
        User user = insertTestingUser();
        Game originalGame = insertTestingGame();
        Game replacementGame = gamesRepo.save(new Game("Replacement Game"));

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
        User user = insertTestingUser();
        Game game = insertTestingGame();

        boolean result = gamesOwnedService.updateGameReferences(null, user, game);

        assertFalse("updateGameReferencesNullGameIDTest: should reject a null ID",
                result);
    }

    /**
     * Verifies that a null user is rejected.
     */
    @Test
    public void updateGameReferencesNullUserTest() {
        Game game = insertTestingGame();

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
        User user = insertTestingUser();
        Game game = insertTestingGame();
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
        User user = insertTestingUser();
        Game originalGame = insertTestingGame();
        Game replacementGame = gamesRepo.save(new Game("Replacement Game"));
        GameOwned ownedGame = insertTestingOwnedGame(user, originalGame, 2020);

        boolean result = gamesOwnedService.editGameInCollection(
                user, ownedGame.getGameOwnedID(), replacementGame, 2021);

        assertTrue("editGameInCollectionSuccessTest: should update the record", result);

        GameOwned updatedGame = gamesOwnedService.getGameOwnedByID(
                user, ownedGame.getGameOwnedID());

        assertEquals("editGameInCollectionSuccessTest: should update the game",
                replacementGame.getGameID(), updatedGame.getGame().getGameID());
        assertEquals("editGameInCollectionSuccessTest: should update the year",
                Integer.valueOf(2021), updatedGame.getYearProduced());
        assertEquals("editGameInCollectionSuccessTest: should preserve the owner",
                user, updatedGame.getUser());
        assertEquals("editGameInCollectionSuccessTest: should preserve the record count",
                1, gamesOwnedService.getGamesOwnedByUsername(user).size());
    }

    /**
     * Verifies that an existing production year can be cleared.
     */
    @Test
    public void editGameInCollectionNullYearTest() {
        User user = insertTestingUser();
        Game game = insertTestingGame();
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
        User user = insertTestingUser();
        Game game = insertTestingGame();
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
        User user = insertTestingUser();
        Game game = insertTestingGame();

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
        User user = insertTestingUser();
        Game game = insertTestingGame();
        GameOwned ownedGame = insertTestingOwnedGame(user, game, 2020);

        boolean result = gamesOwnedService.editGameInCollection(
                user, ownedGame.getGameOwnedID(), null, 2021);

        assertFalse("editGameInCollectionNullGameTest: should reject a null game",
                result);
        assertEquals("editGameInCollectionNullGameTest: should preserve the year",
                Integer.valueOf(2020), ownedGame.getYearProduced());
    }

    /**
     * Verifies that editing a nonexistent ownership record fails.
     */
    @Test
    public void editGameInCollectionNonexistentIDTest() {
        User user = insertTestingUser();
        Game game = insertTestingGame();

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
        User user = insertTestingUser();
        User otherUser = userService.newUser(
                "ownedtestuser2", password, password);
        Game originalGame = insertTestingGame();
        Game replacementGame = gamesRepo.save(new Game("Replacement Game"));
        GameOwned ownedGame = insertTestingOwnedGame(user, originalGame, 2020);

        boolean result = gamesOwnedService.editGameInCollection(
                otherUser, ownedGame.getGameOwnedID(), replacementGame, 2021);

        assertFalse("editGameInCollectionWrongUserTest: should reject another user's record",
                result);

        GameOwned unchangedGame = gamesOwnedService.getGameOwnedByID(
                user, ownedGame.getGameOwnedID());

        assertEquals("editGameInCollectionWrongUserTest: should preserve the game",
                originalGame.getGameID(), unchangedGame.getGame().getGameID());
        assertEquals("editGameInCollectionWrongUserTest: should preserve the year",
                Integer.valueOf(2020), unchangedGame.getYearProduced());
    }

    /**
     * Verifies that an early year is rejected before either field is changed.
     */
    @Test
    public void editGameInCollectionYearTooEarlyTest() {
        User user = insertTestingUser();
        Game originalGame = insertTestingGame();
        Game replacementGame = gamesRepo.save(new Game("Replacement Game"));
        GameOwned ownedGame = insertTestingOwnedGame(user, originalGame, 2020);

        boolean result = gamesOwnedService.editGameInCollection(
                user, ownedGame.getGameOwnedID(), replacementGame, -2001);

        assertFalse("editGameInCollectionYearTooEarlyTest: should reject an early year",
                result);

        GameOwned unchangedGame = gamesOwnedService.getGameOwnedByID(
                user, ownedGame.getGameOwnedID());

        assertEquals("editGameInCollectionYearTooEarlyTest: should preserve the game",
                originalGame.getGameID(), unchangedGame.getGame().getGameID());
        assertEquals("editGameInCollectionYearTooEarlyTest: should preserve the year",
                Integer.valueOf(2020), unchangedGame.getYearProduced());
    }

    /**
     * Verifies that a future year is rejected before either field is changed.
     */
    @Test
    public void editGameInCollectionYearTooLateTest() {
        User user = insertTestingUser();
        Game originalGame = insertTestingGame();
        Game replacementGame = gamesRepo.save(new Game("Replacement Game"));
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
        assertEquals("editGameInCollectionYearTooLateTest: should preserve the year",
                Integer.valueOf(2020), unchangedGame.getYearProduced());
    }

    /**
     * Verifies that editing allows the earliest valid production year.
     */
    @Test
    public void editGameInCollectionMinimumYearTest() {
        User user = insertTestingUser();
        Game game = insertTestingGame();
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
        User user = insertTestingUser();
        Game game = insertTestingGame();
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
     * Additional Family Games Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that a family without memberships has no owned games.
     */
    @Test
    public void getGamesOwnedByFamilyNoMembersTest() {
        User user = insertTestingUser();
        Family family = familiesRepo.save(new Family("Test Family", user));
        insertTestingOwnedGame(user, insertTestingGame(), 2020);

        List<GameOwned> result = gamesOwnedService.getGamesOwnedByFamily(family);

        assertNotNull("getGamesOwnedByFamilyNoMembersTest: should return a list",
                result);
        assertTrue("getGamesOwnedByFamilyNoMembersTest: should return an empty list",
                result.isEmpty());
    }

    /**
     * Verifies that only accepted members of the selected family contribute
     * games, including multiple copies of the same game.
     */
    @Test
    public void getGamesOwnedByFamilyAcceptedMembersTest() {
        User user = insertTestingUser();
        User secondMember = userService.newUser(
                "ownedtestuser2", password, password);
        User pendingMember = userService.newUser(
                "ownedtestuser3", password, password);
        User outsider = userService.newUser(
                "ownedtestuser4", password, password);

        Family family = familiesRepo.save(new Family("Test Family", user));
        Family otherFamily = familiesRepo.save(new Family("Other Family", outsider));

        membershipRepo.save(new FamilyMembership(family, user, "ACCEPTED"));
        membershipRepo.save(new FamilyMembership(family, secondMember, "ACCEPTED"));
        membershipRepo.save(new FamilyMembership(family, pendingMember, "PENDING"));
        membershipRepo.save(new FamilyMembership(otherFamily, outsider, "ACCEPTED"));

        Game game = insertTestingGame();
        GameOwned firstCopy = insertTestingOwnedGame(user, game, 2020);
        GameOwned secondCopy = insertTestingOwnedGame(user, game, 2021);
        GameOwned memberCopy = insertTestingOwnedGame(secondMember, game, 2022);
        GameOwned pendingCopy = insertTestingOwnedGame(pendingMember, game, 2020);
        GameOwned outsiderCopy = insertTestingOwnedGame(outsider, game, 2020);

        List<GameOwned> result = gamesOwnedService.getGamesOwnedByFamily(family);

        assertEquals("getGamesOwnedByFamilyAcceptedMembersTest: should return three records",
                3, result.size());
        assertTrue("getGamesOwnedByFamilyAcceptedMembersTest: should include the first copy",
                result.contains(firstCopy));
        assertTrue("getGamesOwnedByFamilyAcceptedMembersTest: should include the second copy",
                result.contains(secondCopy));
        assertTrue("getGamesOwnedByFamilyAcceptedMembersTest: should include the other accepted member",
                result.contains(memberCopy));
        assertFalse("getGamesOwnedByFamilyAcceptedMembersTest: should exclude pending members",
                result.contains(pendingCopy));
        assertFalse("getGamesOwnedByFamilyAcceptedMembersTest: should exclude another family's member",
                result.contains(outsiderCopy));
    }

    /**
     * Adds one ownership record and retrieves it using its generated ID.
     */
    private GameOwned insertTestingOwnedGame(User user, Game game, Integer yearProduced) {
        List<GameOwned> previousGames =
                gamesOwnedService.getGamesOwnedByUsername(user);
        List<Long> previousIDs = previousGames.stream()
                .map(GameOwned::getGameOwnedID)
                .toList();

        boolean result = gamesOwnedService.addGameToCollection(
                user, game.getGameID(), yearProduced);

        assertTrue("insertTestingOwnedGame: should add the test ownership record",
                result);

        return gamesOwnedService.getGamesOwnedByUsername(user).stream()
                .filter(ownedGame -> !previousIDs.contains(ownedGame.getGameOwnedID()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "insertTestingOwnedGame: could not find the new ownership record"));
    }

    /**
     * Verifies that retrieving games for a null family returns an empty list.
     */
    @Test
    public void getGamesOwnedByFamilyNullFamilyTest() {
        List<GameOwned> result = gamesOwnedService.getGamesOwnedByFamily(null);

        assertNotNull("getGamesOwnedByFamilyNullFamilyTest: result should not be null", result);
        assertTrue("getGamesOwnedByFamilyNullFamilyTest: result should be empty", result.isEmpty());
    }

    private User insertTestingUser() {
        return userService.newUser(username, password, password);
    }

    private Game insertTestingGame() {
        return gamesRepo.save(new Game("OwnedTestGame"));
    }
}
