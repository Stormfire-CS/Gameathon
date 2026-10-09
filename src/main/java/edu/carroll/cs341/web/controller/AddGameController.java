package edu.carroll.cs341.web.controller;

import edu.carroll.cs341.jpa.repo.GamesRepository;
import edu.carroll.cs341.service.GamesService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AddGameController {

    GamesService gamesService;

    AddGameController(GamesService gamesService) {
        this.gamesService = gamesService;
    }

    @GetMapping("/addGamePage")
    public String addGame (String username, Model model) {
        model.addAttribute("username", username);
        return "addGame";
    }

    @PostMapping("/addGame")
    public String addGame(String username, String gameName, Integer minPlayers, Integer maxPlayers) {
        gamesService.addGame(username, gameName, minPlayers, maxPlayers);

        return "redirect:/addGameToCollectionPage?username=" + username;
    }


}
