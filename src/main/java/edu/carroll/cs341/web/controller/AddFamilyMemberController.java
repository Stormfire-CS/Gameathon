package edu.carroll.cs341.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

@Controller
public class AddFamilyMemberController {

    @GetMapping("/addFamilyPage")
    public String addFamilyPage(String username, Model model){
        model.addAttribute("username", username);
        return "addFamilyMember";
    }

}
