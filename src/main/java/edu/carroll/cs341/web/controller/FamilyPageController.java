package edu.carroll.cs341.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;


@Controller
public class FamilyPageController {

    @GetMapping("/familyPage")
    public String familyPage(String username, Model model) {
        model.addAttribute("username", username);
        return "familyPage";
    }
}
