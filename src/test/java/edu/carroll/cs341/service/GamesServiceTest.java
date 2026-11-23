package edu.carroll.cs341.service;

import edu.carroll.cs341.jpa.model.Game;
import edu.carroll.cs341.jpa.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertFalse;
import static org.springframework.test.util.AssertionErrors.assertNotEquals;
import static org.springframework.test.util.AssertionErrors.assertNotNull;
import static org.springframework.test.util.AssertionErrors.assertNull;
import static org.springframework.test.util.AssertionErrors.assertTrue;

/**
 * Tests the {@link GamesService} methods responsible for adding, looking up, editing,
 * and hiding catalog games.
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
public class GamesServiceTest {

    @Autowired
    private GamesService gamesService;

    @Autowired
    private GamesOwnedService gamesOwnedService;

    @Autowired
    private UserService userService;

    @PersistenceContext
    private EntityManager entityManager;

    /*
     * = = = = = = = = = = =
     * Add Game Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that valid bounds, equal bounds, zero, and unspecified bounds are saved.
     */
    @Test
    public void addGameSuccessTest() {
        final String username = "testuser";
        final String password = "testpass";
        final int smaller = 1;
        final int larger = 10;
        final String test = "addGameSuccessTest";

        User user = insertTestingUser(username, password);

        assertAddedGame(test, user, "Regular Game", smaller, larger);
        assertAddedGame(test, user, "Single Player Game", smaller, smaller);
        assertAddedGame(test, user, "Zero Players Game", 0, 0);
        assertAddedGame(test, user, "No Maximum Game", smaller, null);
        assertAddedGame(test, user, "No Minimum Game", null, smaller);
        assertAddedGame(test, user, "No Player Limits Game", null, null);
    }

    /**
     * Verifies that negative and reversed bounds are rejected before and after a game exists.
     */
    @Test
    public void addGameInvalidPlayerCountsTest() {
        final String username = "testuser";
        final String password = "testpass";
        final String validName = "free";
        final int smaller = 1;
        final int larger = 10;
        final String test = "addGameInvalidPlayerCountsTest";

        User user = insertTestingUser(username, password);
        List<Game> initialCatalog = gamesService.getAvailableGames(user);

        // No user-owned games in the catalog
        assertAddGameRejectsInvalidPlayerCounts(test, user, validName, smaller, larger);
        assertEquals(test + ": invalid counts should leave the initial catalog intact",
                initialCatalog, gamesService.getAvailableGames(user));

        // One user-owned game in the catalog
        Game existingGame = insertTestingGame("Existing Game", user, smaller, larger);
        assertAddGameRejectsInvalidPlayerCounts(test, user, validName, smaller, larger);
        List<Game> catalog = gamesService.getAvailableGames(user);
        assertEquals(test + ": invalid counts should leave the catalog size unchanged", initialCatalog.size() + 1, catalog.size());
        assertTrue(test + ": the existing game should still be visible", catalog.contains(existingGame));
        assertNull(test + ": the invalid game should not be visible", findAvailableGame(user, validName));
    }

    /**
     * Verifies that a visible user game cannot be added again with any player bounds.
     */
    @Test
    public void addGameExistingUserGameTest() {
        final String username = "testuser";
        final String password = "testpass";
        final int smaller = 1;
        final int larger = 10;
        final String test = "addGameExistingUserGameTest";
        final String gameName = "Existing Game";

        User user = insertTestingUser(username, password);
        Game existingGame = insertTestingGame(gameName, user, smaller, larger);
        List<Game> initialCatalog = gamesService.getAvailableGames(user);

        assertAddGameRejectsAllPlayerCounts(test, user, gameName, smaller, larger);
        boolean result = gamesService.addGame(user, "  EXISTING GAME  ", smaller, larger);
        assertFalse(test + ": should reject the same name with different case and surrounding spaces", result);
        assertEquals(test + ": the catalog should still contain the original games", initialCatalog, gamesService.getAvailableGames(user));
        assertGameDetails(test, existingGame, gameName, user, smaller, larger, true);
    }

    /**
     * Verifies that a user cannot add a game with an existing public name.
     */
    @Test
    public void addGameExistingPublicGameTest() {
        final String username = "testuser";
        final String password = "testpass";
        final int smaller = 1;
        final int larger = 10;
        final String gameName = "Public Game";

        User user = insertTestingUser(username, password);
        Game publicGame = insertTestingPublicGame(gameName, smaller, larger);

        boolean result = gamesService.addGame(user, "  PUBLIC GAME  ", smaller, larger);
        assertFalse("addGameExistingPublicGameTest: should reject a public duplicate after trimming and ignoring case", result);
        assertEquals("addGameExistingPublicGameTest: should keep the public game visible",
                publicGame, findAvailableGame(user, gameName));
    }

    /**
     * Verifies that a null game name is rejected for every player-count combination.
     */
    @Test
    public void addGameNullNameTest() {
        final String username = "testuser";
        final String password = "testpass";
        final int smaller = 1;
        final int larger = 10;
        final String test = "addGameNullNameTest";

        User user = insertTestingUser(username, password);
        List<Game> initialCatalog = gamesService.getAvailableGames(user);

        assertAddGameRejectsAllPlayerCounts(test, user, null, smaller, larger);
        assertEquals(test + ": should preserve the catalog", initialCatalog, gamesService.getAvailableGames(user));
    }

    /**
     * Verifies that a null user cannot add a new, existing, or unnamed game.
     */
    @Test
    public void addGameNullUserTest() {
        final String username = "testuser";
        final String password = "testpass";
        final String validName = "free";
        final int smaller = 1;
        final int larger = 10;
        final String test = "addGameNullUserTest";
        final String publicName = "Public Game";

        User user = insertTestingUser(username, password);
        insertTestingPublicGame(publicName, smaller, larger);
        List<Game> initialCatalog = gamesService.getAvailableGames(user);

        assertAddGameRejectsAllPlayerCounts(test, null, validName, smaller, larger);
        assertAddGameRejectsAllPlayerCounts(test, null, publicName, smaller, larger);
        assertAddGameRejectsAllPlayerCounts(test, null, null, smaller, larger);
        assertEquals(test + ": should preserve the catalog", initialCatalog, gamesService.getAvailableGames(user));
    }

    /**
     * Verifies that a name containing only whitespace or no characters is rejected.
     */
    @Test
    public void addGameBlankNameTest() {
        final String username = "testuser";
        final String password = "testpass";
        final int smaller = 1;
        final int larger = 10;

        User user = insertTestingUser(username, password);
        List<Game> initialCatalog = gamesService.getAvailableGames(user);

        boolean result = gamesService.addGame(user, "", smaller, larger);
        assertFalse("addGameBlankNameTest: should reject an empty name", result);
        result = gamesService.addGame(user, "  \t  ", smaller, larger);
        assertFalse("addGameBlankNameTest: should reject whitespace only", result);
        assertEquals("addGameBlankNameTest: should preserve the catalog", initialCatalog, gamesService.getAvailableGames(user));
    }

    /**
     * Verifies that leading and trailing spaces are trimmed and users' catalogs remain independent.
     */
    @Test
    public void addGameNameWithSpacesTest() {
        final String username = "testuser";
        final String password = "testpass";
        final int smaller = 1;
        final int larger = 10;
        final String test = "addGameNameWithSpacesTest";

        User user = insertTestingUser(username, password);
        User otherUser = insertTestingUser("otheruser", password);

        assertAddedGame(test, user, "Trailing Game ", smaller, larger);
        assertAddedGame(test, user, " Leading Game", smaller, larger);
        Game firstGame = assertAddedGame(test, user, "  Shared Game  ", smaller, larger);
        Game secondGame = assertAddedGame(test, otherUser, "  Shared Game  ", 2, 4);
        assertNotEquals(test + ": users should have independent game IDs", firstGame.getGameID(), secondGame.getGameID());
        assertGameDetails(test, findAvailableGame(user, "Shared Game"), "Shared Game", user, smaller, larger, true);
    }

    /**
     * Verifies that adding a hidden game restores the same record with updated bounds.
     */
    @Test
    public void addGameRestoresHiddenGameTest() {
        final String username = "testuser";
        final String password = "testpass";
        final int smaller = 1;
        final int larger = 10;
        final String test = "addGameRestoresHiddenGameTest";
        final String gameName = "Hidden Game";

        User user = insertTestingUser(username, password);
        Game hiddenGame = insertTestingCatalogEntry(gameName, user, false, smaller, larger);
        Long originalID = hiddenGame.getGameID();
        assertNull(test + ": the hidden game should be absent from the catalog", findAvailableGame(user, gameName));

        Game restored = assertAddedGame(test, user, gameName, 2, 6);
        assertEquals(test + ": should restore the original record", originalID, restored.getGameID());
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
        var emptyResult = result.isEmpty();
        assertTrue("getAvailableGamesNullUserTest: should return an empty list",
                emptyResult);
    }

    /**
     * Verifies that public games and the user's visible games are included,
     * while hidden games and another user's games are excluded.
     */
    @Test
    public void getAvailableGamesVisibleGamesTest() {
        final String username = "testuser";
        final String password = "testpass";
        final int smaller = 1;
        final int larger = 10;

        User user = insertTestingUser(username, password);
        User otherUser = insertTestingUser("otheruser", password);

        // Existing catalog before adding this test's games
        List<Game> emptyCatalog = gamesService.getAvailableGames(user);
        int initialGameCount = emptyCatalog.size();

        // One public game
        Game publicGame = insertTestingPublicGame("Public Game", smaller, larger);
        List<Game> publicCatalog = gamesService.getAvailableGames(user);
        int gameCount = publicCatalog.size();
        assertEquals("getAvailableGamesVisibleGamesTest: should add one visible public game", initialGameCount + 1, gameCount);
        boolean containsPublicGame = publicCatalog.contains(publicGame);
        assertTrue("getAvailableGamesVisibleGamesTest: should return the public game", containsPublicGame);

        // Public, private, and hidden games
        Game userGame = insertTestingGame("User Game", user, smaller, larger);
        Game hiddenGame = insertTestingCatalogEntry("Hidden Game", user, false, smaller, larger);
        Game otherGame = insertTestingGame("Other Game", otherUser, smaller, larger);

        List<Game> result = gamesService.getAvailableGames(user);

        var containsExpectedItem = result.contains(publicGame);
        assertTrue("getAvailableGamesVisibleGamesTest: should include public games",
                containsExpectedItem);
        var containsExpectedItem2 = result.contains(userGame);
        assertTrue("getAvailableGamesVisibleGamesTest: should include the user's visible games",
                containsExpectedItem2);
        var containsExpectedItem3 = result.contains(hiddenGame);
        assertFalse("getAvailableGamesVisibleGamesTest: should exclude hidden games",
                containsExpectedItem3);
        boolean hasOtherGame = result.contains(otherGame);
        assertFalse("getAvailableGamesVisibleGamesTest: should exclude another user's games", hasOtherGame);

        result = gamesService.getAvailableGames(otherUser);
        gameCount = result.size();
        assertEquals("getAvailableGamesVisibleGamesTest: second user should see two additional games", initialGameCount + 2, gameCount);
        boolean hasPublicGame = result.contains(publicGame);
        assertTrue("getAvailableGamesVisibleGamesTest: second user should see the public game", hasPublicGame);
        hasOtherGame = result.contains(otherGame);
        assertTrue("getAvailableGamesVisibleGamesTest: second user should see their own game", hasOtherGame);
    }

    /**
     * Verifies that a user's version replaces the public game of the same name.
     */
    @Test
    public void getAvailableGamesUserCopyOverridesPublicGameTest() {
        final String username = "testuser";
        final String password = "testpass";
        final int smaller = 1;
        final int larger = 10;

        User user = insertTestingUser(username, password);
        Game publicGame = insertTestingPublicGame("Shared Game", smaller, larger);
        Game userGame = insertTestingUserCopy(publicGame, user, true, smaller, larger);

        List<Game> result = gamesService.getAvailableGames(user);

        var containsExpectedItem = result.contains(userGame);
        assertTrue("getAvailableGamesUserCopyOverridesPublicGameTest: should include the user's copy",
                containsExpectedItem);
        var containsExpectedItem2 = result.contains(publicGame);
        assertFalse("getAvailableGamesUserCopyOverridesPublicGameTest: should exclude the public copy",
                containsExpectedItem2);
    }

    /**
     * Verifies that a hidden user copy also hides the corresponding public game.
     */
    @Test
    public void getAvailableGamesHiddenCopyHidesPublicGameTest() {
        final String username = "testuser";
        final String password = "testpass";
        final int smaller = 1;
        final int larger = 10;

        User user = insertTestingUser(username, password);
        Game publicGame = insertTestingPublicGame("Shared Game", smaller, larger);
        Game hiddenGame = insertTestingUserCopy(publicGame, user, false, smaller, larger);

        List<Game> result = gamesService.getAvailableGames(user);

        var containsExpectedItem = result.contains(publicGame);
        assertFalse("getAvailableGamesHiddenCopyHidesPublicGameTest: should exclude the public copy",
                containsExpectedItem);
        var containsExpectedItem2 = result.contains(hiddenGame);
        assertFalse("getAvailableGamesHiddenCopyHidesPublicGameTest: should exclude the hidden copy",
                containsExpectedItem2);
    }

    /**
     * Verifies that available games are sorted alphabetically without
     * treating uppercase and lowercase letters differently.
     */
    @Test
    public void getAvailableGamesAlphabeticalOrderTest() {
        final String username = "testuser";
        final String password = "testpass";
        final int smaller = 1;
        final int larger = 10;

        User user = insertTestingUser(username, password);
        Game zebra = insertTestingGame("zebra", user, smaller, larger);
        Game apple = insertTestingGame("apple", user, smaller, larger);
        Game banana = insertTestingGame("Banana", user, smaller, larger);

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
        final String validName = "free";
        final int smaller = 1;
        final int larger = 10;

        Game game = insertTestingPublicGame(validName, smaller, larger);

        Game result = gamesService.getGameByGameID(game.getGameID());

        assertNotNull("getGameByGameIDSuccessTest: should find the game", result);
        var actualGameID = result.getGameID();
        assertEquals("getGameByGameIDSuccessTest: should return the matching game",
                game.getGameID(), actualGameID);
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
        final String username = "testuser";
        final String password = "testpass";
        final String validName = "free";
        final int smaller = 1;
        final int larger = 10;

        User user = insertTestingUser(username, password);
        Game game = insertTestingGame(validName, user, smaller, larger);

        boolean result = gamesService.editGame(
                user, game.getGameID(), validName, 2, 6);

        assertTrue("editGameSameNameSuccessTest: should update the game", result);

        Game updatedGame = gamesService.getGameByGameID(game.getGameID());

        var actualMinPlayers = updatedGame.getMinPlayers();
        assertEquals("editGameSameNameSuccessTest: should update minimum players",
                Integer.valueOf(2), actualMinPlayers);
        var actualMaxPlayers = updatedGame.getMaxPlayers();
        assertEquals("editGameSameNameSuccessTest: should update maximum players",
                Integer.valueOf(6), actualMaxPlayers);
        var actualCanSee = updatedGame.getCanSee();
        assertTrue("editGameSameNameSuccessTest: should keep the game visible",
                actualCanSee);
    }

    /**
     * Verifies that editing a public game creates a user copy and preserves
     * the public game's player counts.
     */
    @Test
    public void editGamePublicGameCreatesUserCopyTest() {
        final String username = "testuser";
        final String password = "testpass";
        final String validName = "free";
        final int smaller = 1;
        final int larger = 10;

        User user = insertTestingUser(username, password);
        Game publicGame = insertTestingPublicGame(validName, smaller, larger);

        boolean result = gamesService.editGame(
                user, publicGame.getGameID(), validName, 2, 6);

        assertTrue("editGamePublicGameCreatesUserCopyTest: should edit the game", result);

        Game userCopy = findAvailableGame(user, validName);

        assertNotNull("editGamePublicGameCreatesUserCopyTest: should create a user copy",
                userCopy);
        var actualGameID = userCopy.getGameID();
        assertNotEquals("editGamePublicGameCreatesUserCopyTest: should create a separate record",
                publicGame.getGameID(), actualGameID);
        var actualMinPlayers = userCopy.getMinPlayers();
        assertEquals("editGamePublicGameCreatesUserCopyTest: should set the user's minimum",
                Integer.valueOf(2), actualMinPlayers);
        var actualMaxPlayers = userCopy.getMaxPlayers();
        assertEquals("editGamePublicGameCreatesUserCopyTest: should set the user's maximum",
                Integer.valueOf(6), actualMaxPlayers);
        var actualMinPlayers2 = publicGame.getMinPlayers();
        assertEquals("editGamePublicGameCreatesUserCopyTest: should preserve the public minimum",
                smaller, actualMinPlayers2);
        var actualMaxPlayers2 = publicGame.getMaxPlayers();
        assertEquals("editGamePublicGameCreatesUserCopyTest: should preserve the public maximum",
                larger, actualMaxPlayers2);
    }

    /**
     * Verifies that renaming a user's game hides the original and creates
     * a visible game with the new name.
     */
    @Test
    public void editGameNewNameSuccessTest() {
        final String username = "testuser";
        final String password = "testpass";
        final String validName = "free";
        final int smaller = 1;
        final int larger = 10;

        User user = insertTestingUser(username, password);
        Game game = insertTestingGame(validName, user, smaller, larger);

        boolean result = gamesService.editGame(
                user, game.getGameID(), "Renamed Game", 2, 6);

        assertTrue("editGameNewNameSuccessTest: should rename the game", result);

        Game renamedGame = findAvailableGame(user, "Renamed Game");

        assertNotNull("editGameNewNameSuccessTest: should save the renamed game",
                renamedGame);
        var actualCanSee = renamedGame.getCanSee();
        assertTrue("editGameNewNameSuccessTest: should make the renamed game visible",
                actualCanSee);
        assertFalse("editGameNewNameSuccessTest: should hide the original game",
                gamesService.getGameByGameID(game.getGameID()).getCanSee());
        var actualMinPlayers = renamedGame.getMinPlayers();
        assertEquals("editGameNewNameSuccessTest: should save minimum players",
                Integer.valueOf(2), actualMinPlayers);
        var actualMaxPlayers = renamedGame.getMaxPlayers();
        assertEquals("editGameNewNameSuccessTest: should save maximum players",
                Integer.valueOf(6), actualMaxPlayers);
    }

    /**
     * Verifies that spaces around an updated game name are removed.
     */
    @Test
    public void editGameNameWithSpacesTest() {
        final String username = "testuser";
        final String password = "testpass";
        final String validName = "free";
        final int smaller = 1;
        final int larger = 10;

        User user = insertTestingUser(username, password);
        Game game = insertTestingGame(validName, user, smaller, larger);

        boolean result = gamesService.editGame(
                user, game.getGameID(), "  Renamed Game  ", smaller, larger);

        assertTrue("editGameNameWithSpacesTest: should accept the trimmed name", result);
        assertNotNull("editGameNameWithSpacesTest: should save the trimmed name",
                findAvailableGame(user, "Renamed Game"));
    }

    /**
     * Verifies that renaming a public game is isolated to the user who edited it.
     */
    @Test
    public void editGamePublicGameNewNameTest() {
        final String username = "testuser";
        final String password = "testpass";
        final String validName = "free";
        final int smaller = 1;
        final int larger = 10;
        final String test = "editGamePublicGameNewNameTest";

        User user = insertTestingUser(username, password);
        User otherUser = insertTestingUser("otheruser", password);
        Game publicGame = insertTestingPublicGame(validName, smaller, larger);

        boolean result = gamesService.editGame(user, publicGame.getGameID(), "Renamed Game", null, null);
        assertTrue(test + ": should rename the game with unspecified player counts", result);
        Game renamedGame = findAvailableGame(user, "Renamed Game");
        assertGameDetails(test, renamedGame, "Renamed Game", user, null, null, true);
        assertNull(test + ": original name should be hidden for the editor", findAvailableGame(user, validName));
        assertEquals(test + ": the other user should still see the public game",
                publicGame, findAvailableGame(otherUser, validName));
        assertNull(test + ": the other user should not see the renamed game",
                findAvailableGame(otherUser, "Renamed Game"));
    }

    /**
     * Verifies that invalid player counts leave the original game intact.
     */
    @Test
    public void editGameInvalidPlayerCountsTest() {
        final String username = "testuser";
        final String password = "testpass";
        final String validName = "free";
        final int smaller = 1;
        final int larger = 10;
        final String test = "editGameInvalidPlayerCountsTest";

        User user = insertTestingUser(username, password);
        Game game = insertTestingGame(validName, user, smaller, larger);
        Long gameID = game.getGameID();

        boolean result = gamesService.editGame(user, gameID, validName, larger, smaller);
        assertFalse(test + ": should reject a minimum greater than the maximum", result);
        result = gamesService.editGame(user, gameID, validName, -1, larger);
        assertFalse(test + ": should reject a negative minimum", result);
        result = gamesService.editGame(user, gameID, validName, smaller, -1);
        assertFalse(test + ": should reject a negative maximum", result);
        result = gamesService.editGame(user, gameID, validName, -1, null);
        assertFalse(test + ": should reject a negative minimum with no maximum", result);
        result = gamesService.editGame(user, gameID, validName, null, -1);
        assertFalse(test + ": should reject a negative maximum with no minimum", result);
        result = gamesService.editGame(user, gameID, validName, -1, -1);
        assertFalse(test + ": should reject two negative counts", result);
        assertGameDetails(test, gamesService.getGameByGameID(gameID), validName, user, smaller, larger, true);
    }

    /**
     * Verifies that editing fails when the user is null.
     */
    @Test
    public void editGameNullUserTest() {
        final String validName = "free";
        final int smaller = 1;
        final int larger = 10;

        Game game = insertTestingPublicGame(validName, smaller, larger);

        boolean result = gamesService.editGame(
                null, game.getGameID(), "Renamed Game", smaller, larger);

        assertFalse("editGameNullUserTest: should reject a null user", result);
        var actualGameName = game.getGameName();
        assertEquals("editGameNullUserTest: should preserve the original name",
                validName, actualGameName);
    }

    /**
     * Verifies that editing fails when the game ID is null.
     */
    @Test
    public void editGameNullGameIDTest() {
        final String username = "testuser";
        final String password = "testpass";
        final String validName = "free";
        final int smaller = 1;
        final int larger = 10;

        User user = insertTestingUser(username, password);

        boolean result = gamesService.editGame(
                user, null, validName, smaller, larger);

        assertFalse("editGameNullGameIDTest: should reject a null game ID", result);
    }

    /**
     * Verifies that editing fails when the game does not exist.
     */
    @Test
    public void editGameInvalidGameIDTest() {
        final String username = "testuser";
        final String password = "testpass";
        final String validName = "free";
        final int smaller = 1;
        final int larger = 10;

        User user = insertTestingUser(username, password);

        boolean result = gamesService.editGame(
                user, -1L, validName, smaller, larger);

        assertFalse("editGameInvalidGameIDTest: should reject a nonexistent game", result);
    }

    /**
     * Verifies that editing fails when the new game name is null.
     */
    @Test
    public void editGameNullGameNameTest() {
        final String username = "testuser";
        final String password = "testpass";
        final String validName = "free";
        final int smaller = 1;
        final int larger = 10;

        User user = insertTestingUser(username, password);
        Game game = insertTestingGame(validName, user, smaller, larger);

        boolean result = gamesService.editGame(
                user, game.getGameID(), null, smaller, larger);

        assertFalse("editGameNullGameNameTest: should reject a null name", result);
    }

    /**
     * Verifies that editing fails when the new game name is empty.
     */
    @Test
    public void editGameEmptyGameNameTest() {
        final String username = "testuser";
        final String password = "testpass";
        final String validName = "free";
        final int smaller = 1;
        final int larger = 10;

        User user = insertTestingUser(username, password);
        Game game = insertTestingGame(validName, user, smaller, larger);

        boolean result = gamesService.editGame(
                user, game.getGameID(), "", smaller, larger);

        assertFalse("editGameEmptyGameNameTest: should reject an empty name", result);
    }

    /**
     * Verifies that editing fails when the new name contains only spaces.
     */
    @Test
    public void editGameWhitespaceGameNameTest() {
        final String username = "testuser";
        final String password = "testpass";
        final String validName = "free";
        final int smaller = 1;
        final int larger = 10;

        User user = insertTestingUser(username, password);
        Game game = insertTestingGame(validName, user, smaller, larger);

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
        final String username = "testuser";
        final String password = "testpass";
        final String validName = "free";
        final int smaller = 1;
        final int larger = 10;

        User user = insertTestingUser(username, password);
        Game game = insertTestingGame(validName, user, smaller, larger);
        insertTestingGame("Existing Game", user, smaller, larger);

        boolean result = gamesService.editGame(
                user, game.getGameID(), "EXISTING GAME", smaller, larger);

        assertFalse("editGameExistingUserGameNameTest: should reject a duplicate name",
                result);
        var actualCanSee = game.getCanSee();
        assertTrue("editGameExistingUserGameNameTest: should keep the original visible",
                actualCanSee);
    }

    /**
     * Verifies that a game cannot be renamed to an existing public game.
     */
    @Test
    public void editGameExistingPublicGameNameTest() {
        final String username = "testuser";
        final String password = "testpass";
        final String validName = "free";
        final int smaller = 1;
        final int larger = 10;

        User user = insertTestingUser(username, password);
        Game game = insertTestingGame(validName, user, smaller, larger);
        insertTestingPublicGame("Public Game", smaller, larger);

        boolean result = gamesService.editGame(
                user, game.getGameID(), "Public Game", smaller, larger);

        assertFalse("editGameExistingPublicGameNameTest: should reject a public duplicate",
                result);
        var actualCanSee = game.getCanSee();
        assertTrue("editGameExistingPublicGameNameTest: should keep the original visible",
                actualCanSee);
    }

    /**
     * Verifies that editing a public game updates this user's collection
     * references while preserving another user's references.
     */
    @Test
    public void editGameUpdatesCollectionReferencesTest() {
        final String username = "testuser";
        final String password = "testpass";
        final String validName = "free";
        final int smaller = 1;
        final int larger = 10;

        User user = insertTestingUser(username, password);
        User otherUser = insertTestingUser("otheruser", password);
        Game publicGame = insertTestingPublicGame(validName, smaller, larger);

        assertTrue("editGameUpdatesCollectionReferencesTest: should add the user's copy",
                gamesOwnedService.addGameToCollection(user, publicGame.getGameID(), 2020));
        assertTrue("editGameUpdatesCollectionReferencesTest: should add the other user's copy",
                gamesOwnedService.addGameToCollection(otherUser, publicGame.getGameID(), 2021));

        boolean result = gamesService.editGame(
                user, publicGame.getGameID(), validName, 2, 6);

        assertTrue("editGameUpdatesCollectionReferencesTest: should edit successfully",
                result);

        Game userCopy = findAvailableGame(user, validName);

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
        final String username = "testuser";
        final String password = "testpass";
        final String validName = "free";
        final int smaller = 1;
        final int larger = 10;

        User user = insertTestingUser(username, password);
        Game game = insertTestingGame(validName, user, smaller, larger);

        boolean result = gamesService.removeGame(user, game.getGameID());

        assertTrue("removeGameUserGameSuccessTest: should remove the game", result);

        Game removedGame = gamesService.getGameByGameID(game.getGameID());

        assertNotNull("removeGameUserGameSuccessTest: should preserve the database record",
                removedGame);
        var actualCanSee = removedGame.getCanSee();
        assertFalse("removeGameUserGameSuccessTest: should hide the game",
                actualCanSee);
        assertFalse("removeGameUserGameSuccessTest: should exclude it from available games",
                gamesService.getAvailableGames(user).contains(removedGame));
    }

    /**
     * Verifies that removing a public game hides it for the requesting user
     * while keeping it available to other users.
     */
    @Test
    public void removeGamePublicGameSuccessTest() {
        final String username = "testuser";
        final String password = "testpass";
        final String validName = "free";
        final int smaller = 1;
        final int larger = 10;

        User user = insertTestingUser(username, password);
        User otherUser = insertTestingUser("otheruser", password);
        Game publicGame = insertTestingPublicGame(validName, smaller, larger);

        boolean result = gamesService.removeGame(user, publicGame.getGameID());

        assertTrue("removeGamePublicGameSuccessTest: should remove the game", result);

        List<Game> hiddenCopies = entityManager.createQuery("""
                        select game from Game game where game.gameName = :name and game.userWhoAdded = :user
                        """, Game.class)
                .setParameter("name", validName)
                .setParameter("user", user)
                .getResultList();
        assertEquals("removeGamePublicGameSuccessTest: should create one user copy", 1, hiddenCopies.size());
        Game hiddenCopy = hiddenCopies.getFirst();

        assertNotNull("removeGamePublicGameSuccessTest: should create a user copy",
                hiddenCopy);
        var actualCanSee = hiddenCopy.getCanSee();
        assertFalse("removeGamePublicGameSuccessTest: should hide the user copy",
                actualCanSee);
        var actualCanSee2 = publicGame.getCanSee();
        assertTrue("removeGamePublicGameSuccessTest: should preserve the public game",
                actualCanSee2);
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
        final String validName = "free";
        final int smaller = 1;
        final int larger = 10;

        Game game = insertTestingPublicGame(validName, smaller, larger);

        boolean result = gamesService.removeGame(null, game.getGameID());

        assertFalse("removeGameNullUserTest: should reject a null user", result);
        var actualCanSee = game.getCanSee();
        assertTrue("removeGameNullUserTest: should keep the game visible",
                actualCanSee);
    }

    /**
     * Verifies that removing a game fails when the game ID is null.
     */
    @Test
    public void removeGameNullGameIDTest() {
        final String username = "testuser";
        final String password = "testpass";

        User user = insertTestingUser(username, password);

        boolean result = gamesService.removeGame(user, null);

        assertFalse("removeGameNullGameIDTest: should reject a null game ID", result);
    }

    /**
     * Verifies that removing a game fails when the game does not exist.
     */
    @Test
    public void removeGameInvalidGameIDTest() {
        final String username = "testuser";
        final String password = "testpass";

        User user = insertTestingUser(username, password);

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
        final String username = "testuser";
        final String password = "testpass";
        final String validName = "free";
        final int smaller = 1;
        final int larger = 10;

        User user = insertTestingUser(username, password);
        Game game = insertTestingGame(validName, user, smaller, larger);

        assertTrue("removeGameReferencedByCollectionTest: should add the game to the collection",
                gamesOwnedService.addGameToCollection(user, game.getGameID(), 2020));

        boolean result = gamesService.removeGame(user, game.getGameID());

        assertFalse("removeGameReferencedByCollectionTest: should reject a referenced game",
                result);
        assertTrue("removeGameReferencedByCollectionTest: should keep the game visible",
                gamesService.getGameByGameID(game.getGameID()).getCanSee());
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
     * Adds a visible user game and verifies its stored details.
     *
     * @param name    the name of the game to add
     * @param user    the user who owns the game
     * @param smaller the smaller number in the game
     * @param larger  the larger number in the game
     * @return the game that was added
     */
    private Game insertTestingGame(String name, User user, int smaller, int larger) {
        // Add the game and verify that it was successfully saved.
        return assertAddedGame("insertTestingGame", user, name, smaller, larger);
    }

    /**
     * Persists a public game for tests of the default catalog.
     *
     * @param gameName the name of the game to add
     * @param smaller  the smaller number in the game
     * @param larger   the larger number in the game
     * @return the game that was added
     */
    private Game insertTestingPublicGame(String gameName, int smaller, int larger) {
        // Create a public catalog entry without assigning a user as its owner.
        return insertTestingCatalogEntry(gameName, null, true, smaller, larger);
    }

    /**
     * Creates a separate user copy of a public game, with the specified visibility.
     *
     * @param publicGame the public game to copy
     * @param user       the user who owns the copy
     * @param visible    whether the copy should be visible
     * @param smaller    the smaller number in the game
     * @param larger     the larger number in the game
     * @return the game that was added
     */
    private Game insertTestingUserCopy(Game publicGame, User user, boolean visible, int smaller, int larger) {
        // Create a separate catalog entry using the public game's name and the specified owner and visibility.
        Game userCopy = insertTestingCatalogEntry(publicGame.getGameName(), user, visible, smaller, larger);

        // Verify that the user's copy has a different database ID from the public game.
        assertNotEquals("insertTestingUserCopy: should create a separate record", publicGame.getGameID(), userCopy.getGameID());
        return userCopy;
    }

    /**
     * Persists catalog entries that cannot be created directly by the add operation.
     *
     * @param name    the name of the game to add
     * @param user    the user who owns the game
     * @param visible whether the game should be visible
     * @param smaller the smaller number in the game
     * @param larger  the larger number in the game
     * @return the game that was added
     */
    private Game insertTestingCatalogEntry(String name, User user, boolean visible, int smaller, int larger) {
        // Create a catalog entry and assign its owner, visibility, and player limits.
        Game game = new Game(name);
        game.setUserWhoAdded(user);
        game.setCanSee(visible);
        game.setMinPlayers(smaller);
        game.setMaxPlayers(larger);

        // Persist the entry directly and flush it to the database.
        entityManager.persist(game);
        entityManager.flush();

        // Verify that the entry was saved and can be retrieved with the expected details.
        assertNotNull("insertTestingCatalogEntry: should assign an ID", game.getGameID());
        Game saved = gamesService.getGameByGameID(game.getGameID());
        assertGameDetails("insertTestingCatalogEntry", saved, name, user, smaller, larger, visible);
        assertEquals("insertTestingCatalogEntry: should retrieve the saved ID", game.getGameID(), saved.getGameID());
        return saved;
    }

    /**
     * Adds a game and checks that the catalog exposes the saved record.
     *
     * @param name       the name of the game to add
     * @param user       the user who owns the game
     * @param minPlayers the minimum number of players for the game
     * @param maxPlayers the maximum number of players for the game
     * @return the game that was added
     */
    private Game assertAddedGame(String test, User user, String name, Integer minPlayers, Integer maxPlayers) {
        // Attempt to add the game through the games service.
        boolean result = gamesService.addGame(user, name, minPlayers, maxPlayers);
        assertTrue(test + ": should add " + name, result);

        // Find the newly added game in the user's available games and verify its details.
        Game game = findAvailableGame(user, name.trim());
        assertGameDetails(test, game, name.trim(), user, minPlayers, maxPlayers, true);

        // Retrieve the game by its ID and verify that the saved record matches the visible entry.
        Game saved = gamesService.getGameByGameID(game.getGameID());
        assertGameDetails(test, saved, name.trim(), user, minPlayers, maxPlayers, true);
        assertEquals(test + ": the saved ID should match the visible game", game.getGameID(), saved.getGameID());
        return saved;
    }

    /**
     * Verifies that a game has the expected name, owner, player limits, and visibility.
     *
     * @param test       the name of the test case
     * @param game       the game to verify
     * @param name       the expected name of the game
     * @param user       the expected owner of the game
     * @param minPlayers the expected minimum number of players for the game
     * @param maxPlayers the expected maximum number of players for the game
     * @param visible    whether the game should be visible
     */
    private void assertGameDetails(String test, Game game, String name, User user,
                                   Integer minPlayers, Integer maxPlayers, boolean visible) {
        // Check that the game was retrieved and has a database ID.
        assertNotNull(test + ": should retrieve " + name, game);
        assertNotNull(test + ": game should have an ID", game.getGameID());

        // Verify that all saved game details match the expected values.
        assertEquals(test + ": should save the expected name", name, game.getGameName());
        assertEquals(test + ": should save the expected owner", user, game.getUserWhoAdded());
        assertEquals(test + ": should save minimum players", minPlayers, game.getMinPlayers());
        assertEquals(test + ": should save maximum players", maxPlayers, game.getMaxPlayers());
        assertEquals(test + ": should save the expected visibility", visible, game.getCanSee());
    }

    /**
     * Finds a game by name in the list of games available to the specified user.
     *
     * @param user     the user who owns the game
     * @param gameName the name of the game to find
     * @return the game that was found, or null if no matching game was found
     */
    private Game findAvailableGame(User user, String gameName) {
        // Filter the user's available games to find entries with the requested name.
        List<Game> matches = gamesService.getAvailableGames(user).stream()
                .filter(game -> gameName.equals(game.getGameName()))
                .toList();

        // Ensure that no more than one matching visible game is returned.
        assertTrue("findAvailableGame: should find at most one visible game named " + gameName, matches.size() <= 1);
        return matches.isEmpty() ? null : matches.getFirst();
    }

    /**
     * Verifies that adding a game is rejected for the supplied invalid player-count combinations.
     *
     * @param test    the name of the test case
     * @param user    the user who owns the game
     * @param name    the name of the game to add
     * @param smaller the smaller number in the game
     * @param larger  the larger number in the game
     */
    private void assertAddGameRejectsAllPlayerCounts(String test, User user, String name, int smaller, int larger) {
        // Test invalid minimum and maximum player counts.
        assertAddGameRejectsInvalidPlayerCounts(test, user, name, smaller, larger);

        // Test equal player counts, zero player counts, and missing player-count values.
        assertAddGameRejected(test, user, name, smaller, larger);
        assertAddGameRejected(test, user, name, smaller, smaller);
        assertAddGameRejected(test, user, name, 0, 0);
        assertAddGameRejected(test, user, name, smaller, null);
        assertAddGameRejected(test, user, name, null, smaller);
        assertAddGameRejected(test, user, name, null, null);
    }

    /**
     * Tests additional invalid combinations involving negative or reversed player counts.
     *
     * @param test    the name of the test case
     * @param user    the user who owns the game
     * @param name    the name of the game to add
     * @param smaller the smaller number in the game
     * @param larger  the larger number in the game
     */
    private void assertAddGameRejectsInvalidPlayerCounts(String test, User user, String name, int smaller, int larger) {
        // Verify that negative values and minimum counts greater than maximum counts are rejected.
        assertAddGameRejected(test, user, name, smaller, -1);
        assertAddGameRejected(test, user, name, -1, larger);
        assertAddGameRejected(test, user, name, larger, smaller);
        assertAddGameRejected(test, user, name, -1, null);
        assertAddGameRejected(test, user, name, null, -1);
        assertAddGameRejected(test, user, name, -1, -1);
    }

    /**
     * Attempts to add a game with the supplied details and verifies that the operation is rejected.
     *
     * @param test       the name of the test case
     * @param user       the user who owns the game
     * @param name       the name of the game to add
     * @param minPlayers the minimum number of players in the game
     * @param maxPlayers the maximum number of players in the game
     */
    private void assertAddGameRejected(String test, User user, String name, Integer minPlayers, Integer maxPlayers) {
        // Attempt to add the game using the specified name and player-count values.
        boolean result = gamesService.addGame(user, name, minPlayers, maxPlayers);

        // Verify that the games service rejects the invalid game details.
        assertFalse(test + ": should reject name " + name + " with minimum " + minPlayers
                + " and maximum " + maxPlayers, result);
    }
}
