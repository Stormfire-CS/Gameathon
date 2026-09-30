package edu.carroll.cs341.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/homePage")
    public String homePage(String username, Model model){
        model.addAttribute("username", username);
        return "homePage";
    }
}
