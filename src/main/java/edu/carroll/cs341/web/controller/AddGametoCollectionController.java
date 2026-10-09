package edu.carroll.cs341.web.controller;

import edu.carroll.cs341.service.GamesOwnedService;
import edu.carroll.cs341.service.GamesService;
import edu.carroll.cs341.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AddGametoCollectionController {

    private final GamesService gamesService;
    private final GamesOwnedService gamesOwnedService;
    private final UserService userService;

    public AddGametoCollectionController(GamesService gamesService, GamesOwnedService gamesOwnedService, UserService userService) {
        this.gamesService = gamesService;
        this.gamesOwnedService = gamesOwnedService;
        this.userService = userService;
    }

    @GetMapping("/addGametoCollectionPage")
    public String addGametoCollectionPage(String username, Model model) {
        model.addAttribute("username", username);

        Long ownerID = userService.getUserID(username);

        model.addAttribute("games", gamesService.getAvailableGames(ownerID));
        return "addGametoCollection";
    }

    @PostMapping("/addGametoCollection")
    public String addGametoCollection(String username, Long gameID, Integer yearProduced) {
        gamesOwnedService.addGame(username, gameID, yearProduced);

        return "redirect:/viewGamesPage?username=" + username;
    }

}
