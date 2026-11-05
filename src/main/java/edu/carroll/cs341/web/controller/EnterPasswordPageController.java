package edu.carroll.cs341.web.controller;

import edu.carroll.cs341.jpa.model.User;
import edu.carroll.cs341.service.UserService;
import edu.carroll.cs341.web.form.LoginForm;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class EnterPasswordPageController {

    private final UserService userService;

    public EnterPasswordPageController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/enterPasswordPage")
    public String enterPasswordPageGet(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        // Make the username available to the view for subsequent form submissions.
        model.addAttribute("user", user);
        model.addAttribute("loginForm", new LoginForm());

        return "enterPasswordPage";
    }

    @PostMapping("/enterPasswordPage")
    public String enterPasswordPagePost(@Valid @ModelAttribute LoginForm loginForm, BindingResult result, HttpSession session, HttpSession passwordSession, Model model) {
        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        if (result.hasErrors()) {
            model.addAttribute("user", user);
            return "enterPasswordPage";
        }

        passwordSession.setMaxInactiveInterval(5*60);
        passwordSession.setAttribute("loggedInUser", user);

        return "redirect:/changePasswordPage";

    }
}
