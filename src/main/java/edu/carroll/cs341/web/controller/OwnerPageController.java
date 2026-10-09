package edu.carroll.cs341.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OwnerPageController {

    @GetMapping("/ownerPage")
    public String ownerPage(String username, Model model) {
        model.addAttribute("username", username);
        return "ownerPage";
    }
}
