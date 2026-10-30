package edu.carroll.cs341.service;

import edu.carroll.cs341.jpa.model.Family;
import edu.carroll.cs341.jpa.model.Game;
import edu.carroll.cs341.jpa.model.GameOwned;
import edu.carroll.cs341.jpa.model.User;
import edu.carroll.cs341.jpa.repo.FamilyMembershipRepository;
import edu.carroll.cs341.jpa.repo.GamesOwnedRepository;
import edu.carroll.cs341.jpa.repo.GamesRepository;
import edu.carroll.cs341.jpa.repo.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.springframework.test.util.AssertionErrors.*;

/**
 * Tests the GamesOwnedService class to verify that games can be added to and
 * removed from a user's collection, and that owned games can be retrieved
 * for individual users and families. Also tests how the service handles
 * null values, nonexistent games, invalid ownership IDs, and attempts to
 * remove games belonging to another user.
 *
 * @author Max Montowski
 * ChatGPT assisted with test implementation in conjunction with the author; reviewed by author.
 */
@SpringBootTest
@Transactional
public class GamesOwnedServiceTest {

    private static final String username = "ownedtestuser";
    private static final String password = "ownedtestpass";

    @Autowired
    private GamesOwnedService gamesOwnedService;

    @Autowired
    private GamesOwnedRepository gamesOwnedRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private GamesRepository gamesRepo;

    @Autowired
    private FamilyMembershipRepository membershipRepo;

    private User fakeUser;
    private User otherUser;
    private Game fakeGame;

    /**
     * Creates users and a game for the tests.
     */
    @BeforeEach
    public void beforeTest() {
        fakeUser = userRepo.save(new User(username, password));
        otherUser = userRepo.save(new User("ownedtestuser2", password));

        fakeGame = gamesRepo.save(new Game("OwnedTestGame"));
    }

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
        gamesOwnedService.addGameToCollection(fakeUser, fakeGame.getGameID(), null);
        List<GameOwned> result = gamesOwnedService.getGamesOwnedByUsername(fakeUser);
        assertNotNull("getGamesOwnedByUsernameReturnsListTest: result should not be null", result);
        assertEquals("getGamesOwnedByUsernameReturnsListTest: result should contain the users game", result.getFirst().getGame(), fakeGame);
    }

    /**
     * Verifies that retrieving games for a user without owned games
     * returns an empty list.
     */
    @Test
    public void getGamesOwnedByUsernameEmptyListTest() {
        List<GameOwned> result = gamesOwnedService.getGamesOwnedByUsername(fakeUser);
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
        boolean result = gamesOwnedService.addGameToCollection(fakeUser, fakeGame.getGameID(), 2020);
        assertTrue("addGameToCollectionSuccessTest: should add the game successfully", result);
        List<GameOwned> ownedGames = gamesOwnedService.getGamesOwnedByUsername(fakeUser);
        assertEquals("addGameToCollectionSuccessTest: user should own one game", 1, ownedGames.size());
        assertEquals("addGameToCollectionSuccessTest: production year should match",
                Integer.valueOf(2020), ownedGames.get(0).getYearProduced());
    }

    /**
     * Verifies that a game can be added when its production year is unknown.
     */
    @Test
    public void addGameToCollectionNullYearTest() {
        boolean result = gamesOwnedService.addGameToCollection(fakeUser, fakeGame.getGameID(), null);
        assertTrue("addGameToCollectionNullYearTest: should allow a null production year", result);
        List<GameOwned> ownedGames = gamesOwnedService.getGamesOwnedByUsername(fakeUser);
        assertEquals("addGameToCollectionNullYearTest: user should own one game", 1, ownedGames.size());
        assertNull("addGameToCollectionNullYearTest: production year should be null", ownedGames.get(0).getYearProduced());
    }

    /**
     * Verifies that adding a game with a null user fails.
     */
    @Test
    public void addGameToCollectionNullUserTest() {
        boolean result = gamesOwnedService.addGameToCollection(null, fakeGame.getGameID(), 2020);
        assertFalse("addGameToCollectionNullUserTest: should fail with a null user", result);
    }

    /**
     * Verifies that adding a game with a null game ID fails.
     */
    @Test
    public void addGameToCollectionNullGameIDTest() {
        boolean result = gamesOwnedService.addGameToCollection(fakeUser, null, 2020);
        assertFalse("addGameToCollectionNullGameIDTest: should fail with a null game ID", result);
    }

    /**
     * Verifies that adding a game that does not exist fails.
     */
    @Test
    public void addGameToCollectionNonexistentGameTest() {
        boolean result = gamesOwnedService.addGameToCollection(fakeUser, -1L, 2020);
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
        gamesOwnedService.addGameToCollection(fakeUser, fakeGame.getGameID(), 2020);
        List<GameOwned> ownedGames = gamesOwnedService.getGamesOwnedByUsername(fakeUser);
        Long gameOwnedID = ownedGames.get(0).getGameOwnedID();
        boolean result = gamesOwnedService.removeGameFromCollection(fakeUser, gameOwnedID);
        assertTrue("removeGameFromCollectionSuccessTest: should remove the game successfully", result);
        assertTrue("removeGameFromCollectionSuccessTest: user's collection should be empty",
                gamesOwnedService.getGamesOwnedByUsername(fakeUser).isEmpty());
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
        boolean result = gamesOwnedService.removeGameFromCollection(fakeUser, null);
        assertFalse("removeGameFromCollectionNullIDTest: should fail with a null ownership ID", result);
    }

    /**
     * Verifies that removing a nonexistent ownership record fails.
     */
    @Test
    public void removeGameFromCollectionNonexistentIDTest() {
        boolean result = gamesOwnedService.removeGameFromCollection(fakeUser, -1L);
        assertFalse("removeGameFromCollectionNonexistentIDTest: should fail with a nonexistent ID", result);
    }

    /**
     * Verifies that a user cannot remove a game owned by another user.
     */
    @Test
    public void removeGameFromCollectionWrongUserTest() {
        gamesOwnedService.addGameToCollection(fakeUser, fakeGame.getGameID(), 2020);
        Long gameOwnedID = gamesOwnedService.getGamesOwnedByUsername(fakeUser).get(0).getGameOwnedID();
        boolean result = gamesOwnedService.removeGameFromCollection(otherUser, gameOwnedID);
        assertFalse("removeGameFromCollectionWrongUserTest: should fail when the game belongs to another user",
                result);
        assertEquals("removeGameFromCollectionWrongUserTest: original owner should still own the game",
                1, gamesOwnedService.getGamesOwnedByUsername(fakeUser).size());
    }

    /*
     * = = = = = = = = = = =
     * Get Family Games Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that retrieving games for a null family returns an empty list.
     */
    @Test
    public void getGamesOwnedByFamilyNullFamilyTest() {
        List<GameOwned> result = gamesOwnedService.getGamesOwnedByFamily(null);
        assertNotNull("getGamesOwnedByFamilyNullFamilyTest: result should not be null", result);
        assertTrue("getGamesOwnedByFamilyNullFamilyTest: result should be empty", result.isEmpty());
    }
    
}
