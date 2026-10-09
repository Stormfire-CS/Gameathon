package edu.carroll.cs341.web.controller;

import edu.carroll.cs341.jpa.model.FamilyMembership;
import edu.carroll.cs341.service.FamilyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class CreateFamilyController {

    private final FamilyService familyService;

    public CreateFamilyController(FamilyService familyService) {
        this.familyService = familyService;
    }

    @GetMapping("/createFamilyPage")
    public String createFamilyPage(String username, Model model) {
        model.addAttribute("username", username);
        return "createFamily";
    }

    @PostMapping("/createFamily")
    public String createFamily(String username, String familyName) {
        familyService.createFamily(username, familyName);

        return "redirect:/familyPage?username=" + username;
    }
}
