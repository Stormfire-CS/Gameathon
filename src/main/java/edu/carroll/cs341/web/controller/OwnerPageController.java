package edu.carroll.cs341.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

@Controller
public class OwnerPageController {

    @GetMapping("/ownerPage")
    public String ownerPage(String username, Model model) {
        model.addAttribute("username", username);
        return "ownerPage";
    }
}
