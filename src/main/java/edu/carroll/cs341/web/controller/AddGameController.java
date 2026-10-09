package edu.carroll.cs341.web.controller;

import edu.carroll.cs341.jpa.model.Games;
import edu.carroll.cs341.jpa.model.GamesOwned;
import edu.carroll.cs341.service.GamesOwnedService;
import edu.carroll.cs341.service.GamesService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class AddGameController {

    private final GamesService gamesService;
    private final GamesOwnedService gamesOwnedService;

    public AddGameController(GamesService gamesService, GamesOwnedService gamesOwnedService) {
        this.gamesService = gamesService;
        this.gamesOwnedService = gamesOwnedService;
    }

    @GetMapping("/addGamePage")
    public String addGamePage(String username, Model model) {
        model.addAttribute("username", username);
        model.addAttribute("games", gamesService.getAllGames());
        return "addGame";
    }

    @PostMapping("/addGame")
    public String addGame(String username, Long gameID, Integer yearProduced) {
        gamesOwnedService.addGame(username, gameID, yearProduced);

        return "redirect:/viewGamesPage?username=" + username;
    }

}
