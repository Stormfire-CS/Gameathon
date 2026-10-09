package edu.carroll.cs341.web.controller;

import edu.carroll.cs341.service.FamilyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class JoinFamilyController {

    private final FamilyService familyService;

    public JoinFamilyController(FamilyService familyService) {
        this.familyService = familyService;
    }

    @GetMapping("/joinFamilyPage")
    public String joinFamilyPage(String username, Model model) {
        model.addAttribute("username", username);
        return "joinFamily";
    }

    @PostMapping("/requestToJoinFamily")
    public String requestToJoinFamily(String username, Long familyID) {
        familyService.requestToJoinFamily(username, familyID);
        return "redirect:/familyPage?username=" + username;
    }
}
