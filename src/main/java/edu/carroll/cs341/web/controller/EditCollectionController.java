package edu.carroll.cs341.web.controller;

import edu.carroll.cs341.jpa.model.User;
import edu.carroll.cs341.service.GamesOwnedService;
import edu.carroll.cs341.service.GamesService;
import edu.carroll.cs341.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Handles web requests for adding and removing games from a user's personal collection.
 * This controller displays the available games that a user can add to their
 * collection and processes submissions to add or remove owned games.
 * <p>
 * The controller uses {@link GamesService} to retrieve available games,
 * {@link GamesOwnedService} to manage ownership records, and
 * {@link UserService} to retrieve user account information.
 *
 * <p><b>Sources Used:</b></p>
 * <ul>
 *   <li>ChatGPT – Assisted with reviewing and updating Javadoc comments for this file
 *   in conjunction with Max Montowski.
 *   All comments were reviewed by the author.</li>
 * </ul>
 */
@Controller
public class EditCollectionController {

    /**
     * Service used to retrieve games available in the catalog.
     */
    private final GamesService gamesService;

    /**
     * Service used to add games to a user's collection.
     */
    private final GamesOwnedService gamesOwnedService;

    /**
     * Service used to retrieve user account information.
     */
    private final UserService userService;

    /**
     * Constructs the controller with the services required to manage
     * game availability, user accounts, and game ownership.
     *
     * @param gamesService      service for retrieving available games
     * @param gamesOwnedService service for managing owned games
     * @param userService       service for retrieving user information
     */
    public EditCollectionController(GamesService gamesService, GamesOwnedService gamesOwnedService, UserService userService) {
        this.gamesService = gamesService;
        this.gamesOwnedService = gamesOwnedService;
        this.userService = userService;
    }

    /**
     * Displays the page for editing a user's game collection.
     * Adds the logged-in user, available games, and currently owned games
     * to the model for display in the view.
     *
     * @param session the HTTP session containing the logged-in user
     * @param model   the model used to supply data to the view
     * @return the edit collection view, or a redirect to the login page
     * if no user is logged in
     */
    @GetMapping("/editCollectionPage")
    public String addGametoCollectionPage(HttpSession session, Model model) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        // Make the username available to the view for subsequent form submissions.
        model.addAttribute("user", user);

        // Supply the available games to the view for the user to select from.
        model.addAttribute("games", gamesService.getAvailableGames(user));

        model.addAttribute("ownedGames", gamesOwnedService.getGamesOwnedByUsername(user));

        return "editCollection";
    }

    /**
     * Processes a submission to add a game to the user's collection.
     * Delegates the ownership operation to the service and redirects
     * the user back to the collection page.
     *
     * @param session      the HTTP session containing the logged-in user
     * @param gameID       the ID of the game being added
     * @param yearProduced the year the owned game was produced
     * @return a redirect to the collection page, or the login page
     * if no user is logged in
     */
    @PostMapping("/addGameToCollection")
    public String addGametoCollection(HttpSession session, Long gameID, Integer yearProduced) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        // Delegate creation and persistence of the ownership record to the service.
        gamesOwnedService.addGameToCollection(user, gameID, yearProduced);

        // Redirect to the collection view after processing the submission.
        return "redirect:/editCollectionPage";
    }

    /**
     * Processes a request to remove a game from the user's collection.
     * Delegates the removal operation to the service and redirects
     * the user back to the collection page.
     *
     * @param session     the HTTP session containing the logged-in user
     * @param gameOwnedID the ID of the game ownership record to remove
     * @return a redirect to the collection page, or the login page
     * if no user is logged in
     */
    @PostMapping("/deleteGameFromCollection")
    public String deleteGameFromCollection(HttpSession session, Long gameOwnedID) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        // Delegate removal of the ownership record to the service.
        gamesOwnedService.removeGameFromCollection(user, gameOwnedID);

        return "redirect:/editCollectionPage";
    }

}
