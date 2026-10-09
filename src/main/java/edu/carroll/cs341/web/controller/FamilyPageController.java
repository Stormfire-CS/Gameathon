package edu.carroll.cs341.web.controller;

import edu.carroll.cs341.jpa.model.Families;
import edu.carroll.cs341.service.FamilyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;


@Controller
public class FamilyPageController {

    private final FamilyService familyService;

    public FamilyPageController(FamilyService familyService) {
        this.familyService = familyService;
    }

    @GetMapping("/familyPage")
    public String familyPage(String username, Model model) {
        model.addAttribute("username", username);
        Families family = familyService.getFamilyForUser(username);
        model.addAttribute("family", family);
        model.addAttribute("isFamilyMember", family != null);
        model.addAttribute("isFamilyAdmin", family != null && familyService.isFamilyAdmin(username, family.getFamilyID()));
        return "familyPage";
    }
}
