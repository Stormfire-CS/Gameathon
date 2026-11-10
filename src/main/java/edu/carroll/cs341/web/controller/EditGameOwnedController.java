package edu.carroll.cs341.web.controller;

import edu.carroll.cs341.jpa.model.Game;
import edu.carroll.cs341.jpa.model.GameOwned;
import edu.carroll.cs341.jpa.model.User;
import edu.carroll.cs341.service.GamesOwnedService;
import edu.carroll.cs341.service.GamesService;
import edu.carroll.cs341.service.UserService;
import edu.carroll.cs341.web.form.AddGameToCollectionForm;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class EditGameOwnedController {
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
    public EditGameOwnedController(GamesService gamesService, GamesOwnedService gamesOwnedService, UserService userService) {
        this.gamesService = gamesService;
        this.gamesOwnedService = gamesOwnedService;
        this.userService = userService;
    }

    @GetMapping("/editGameOwnedPage")
    public String editGameOwnedPage(
            HttpSession session,
            @RequestParam Long gameOwnedID,
            Model model) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        System.out.println("GameOwned ID: " + gameOwnedID);
        System.out.println("Logged in user: " + user.getUsername());

        // Find the ownership record belonging to this user.
        GameOwned gameOwned = gamesOwnedService.getGameOwnedByID(user, gameOwnedID);

        System.out.println("Found GameOwned: " + gameOwned);

        if (gameOwned == null) {
            return "redirect:/editCollectionPage";
        }

        model.addAttribute("user", user);
        model.addAttribute("gameOwned", gameOwned);
        model.addAttribute("games", gamesService.getAvailableGames(user));
        model.addAttribute("selectedGameID", gameOwned.getGame().getGameID());

        AddGameToCollectionForm form = new AddGameToCollectionForm();
        form.setYearProduced(gameOwned.getYearProduced());

        model.addAttribute("addGameToCollectionForm", form);

        return "editGameOwned";
    }

    @PostMapping("/editGameInCollection")
    public String editGameInCollection(HttpSession session, Long gameID,
                                       @Valid @ModelAttribute("addGameToCollectionForm") AddGameToCollectionForm form,
                                       BindingResult bindingResult,
                                       Model model, Long gameOwnedID) {
        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        // Find the ownership record belonging to this user.
        GameOwned gameOwned = gamesOwnedService.getGameOwnedByID(user, gameOwnedID);

        if (gameOwned == null) {
            return "redirect:/editCollectionPage";
        }

        // Redisplay the collection page if the production year is invalid.
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("gameOwned", gameOwned);
            model.addAttribute("games", gamesService.getAvailableGames(user));
            model.addAttribute("selectedGameID", gameID);
            return "editGameOwned";
        }

        Game game = gamesService.getGameByGameID(gameID);

        gamesOwnedService.editGameInCollection(user, gameOwnedID, game,form.getYearProduced());

        // Redirect to the collection view after processing the submission.
        return "redirect:/editCollectionPage";
    }
}
