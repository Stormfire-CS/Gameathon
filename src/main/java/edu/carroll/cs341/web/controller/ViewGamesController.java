package edu.carroll.cs341.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

@Controller
public class ViewGamesController {

    @GetMapping("viewGamesPage")
    public String viewGamesPage(String username, Model model) {
        model.addAttribute("username", username);
        return "viewGames";
    }
}
