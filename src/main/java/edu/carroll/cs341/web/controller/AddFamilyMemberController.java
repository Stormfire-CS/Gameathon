package edu.carroll.cs341.web.controller;

import edu.carroll.cs341.service.FamilyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AddFamilyMemberController {

    private final FamilyService familyService;

    public AddFamilyMemberController(FamilyService familyService) {
        this.familyService = familyService;
    }

    @GetMapping("/addFamilyPage")
    public String addFamilyPage(String username, Model model){
        model.addAttribute("username", username);
        model.addAttribute("requests",familyService.getPendingRequests(username));
        return "addFamilyMember";
    }

    @PostMapping("/acceptFamilyMember")
    public String acceptFamilyMember(String username, Long membershipID) {
        familyService.acceptRequest(username, membershipID);

        return "redirect:/addFamilyPage?username=" + username;
    }

    @PostMapping("/rejectFamilyMember")
    public String rejectFamilyMember(String username, Long membershipID) {
        familyService.rejectRequest(username, membershipID);

        return "redirect:/addFamilyPage?username=" + username;
    }
}
