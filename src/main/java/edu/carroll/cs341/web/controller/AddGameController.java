package edu.carroll.cs341.web.controller;

import edu.carroll.cs341.jpa.model.Games;
import edu.carroll.cs341.service.GamesOwnedService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class AddGameController {

    private final GamesOwnedService gamesOwnedService;

    public AddGameController(GamesOwnedService gamesOwnedService) {
        this.gamesOwnedService = gamesOwnedService;
    }

    @GetMapping("/addGamePage")
    public String addGamePage(String username, Model model) {
        model.addAttribute("username", username);
        return "addGame";
    }

    @PostMapping("/addGame")
    public String addGame(String username, String gameName, Integer yearProduced) {
        gamesOwnedService.addGame(username, gameName, yearProduced);

        return "redirect:/viewGamesPage?username=" + username;
    }

    @GetMapping("/searchGames")
    @ResponseBody
    public List<Games> searchGames(@RequestParam String gameName) {
        return gamesOwnedService.findGames(gameName);
    }
}
