package edu.carroll.cs341.web.controller;

import edu.carroll.cs341.service.GamesService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

@Controller
public class ViewGamesController {

    private final GamesService gamesService;

    public ViewGamesController(GamesService gamesService) {
        this.gamesService = gamesService;
    }

    @GetMapping("/viewGamesPage")
    public String viewGamesPage(Long ownerID, String username, Model model) {
        model.addAttribute("username", username);
        model.addAttribute("games", gamesOwnedService.getGamesOwnedByOwner(ownerID));
        return "viewGames";
    }
}
