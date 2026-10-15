package edu.carroll.cs341.web.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class LogoutController {

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        // End the current user's session.
        session.invalidate();

        // Return to the login page.
        return "redirect:/login";
    }
}