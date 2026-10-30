package edu.carroll.cs341.web.controller;

import edu.carroll.cs341.jpa.model.User;
import edu.carroll.cs341.service.GamesOwnedService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Handles web requests for viewing games owned by a user.
 * Retrieves the user's game collection through {@link GamesOwnedService}
 * and makes the collection available to the view.
 *
 * <p><b>Sources Used:</b></p>
 * <ul>
 *   <li>ChatGPT – Assisted with reviewing and updating Javadoc comments for this file
 *   in conjunction with Max Montowski.
 *   All comments were reviewed by the author.</li>
 * </ul>
 */
@Controller
public class ViewGamesController {

    /**
     * Service used to retrieve games owned by a user.
     */
    private final GamesOwnedService gamesOwnedService;

    /**
     * Constructs the controller with the service required to retrieve
     * a user's game collection.
     *
     * @param gamesOwnedService service for retrieving user-owned games
     */
    public ViewGamesController(GamesOwnedService gamesOwnedService) {
        this.gamesOwnedService = gamesOwnedService;
    }

    /**
     * Displays the page containing the logged-in user's games.
     * Adds the user and their game collection to the model so they can
     * be accessed by the view. Redirects to the login page if no user
     * is logged in.
     *
     * @param session the HTTP session containing the logged-in user
     * @param model   the model used to provide attributes to the view
     * @return the view-games page, or a redirect to the login page
     * if no user is logged in
     */
    @GetMapping("/viewGamesPage")
    public String viewGamesPage(HttpSession session, Model model) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        // Make the username available to the view.
        model.addAttribute("user", user);

        // Retrieve the games associated with this username and provide them to the view.
        model.addAttribute("games", gamesOwnedService.getGamesOwnedByUsername(user));

        return "viewGames";
    }
}
