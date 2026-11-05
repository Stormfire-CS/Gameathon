package edu.carroll.cs341.web.controller;

import edu.carroll.cs341.jpa.model.User;
import edu.carroll.cs341.service.UserService;
import edu.carroll.cs341.web.form.EditPasswordForm;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ChangePasswordPageController {

    private final UserService userService;

    public ChangePasswordPageController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/changePasswordPage")
    public String changePassword (HttpSession session, HttpSession passwordSession, Model model) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        User passUser = (User) passwordSession.getAttribute("loggedInUser");

        if (passUser == null) {
            return "redirect:/editAccount";
        }

        model.addAttribute("user", user);
        model.addAttribute("editPasswordForm", new EditPasswordForm());

        return "changePasswordPage";
    }

    @PostMapping("/changePassword")
    public String changePassword(HttpSession session, HttpSession passwordSession, @Valid @ModelAttribute("editPasswordForm") EditPasswordForm editPasswordForm,
                                 BindingResult bindingResult, Model model)
    {
        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        User passUser = (User) passwordSession.getAttribute("loggedInUser");

        if (passUser == null) {
            return "redirect:/editAccount";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            return "changePasswordPage";
        }

        userService.changeUsersPassword(user, editPasswordForm.getPassword1());

        return "redirect:/";
    }
}
