package edu.carroll.cs341.web.controller;

import edu.carroll.cs341.jpa.model.User;
import edu.carroll.cs341.service.UserService;
import edu.carroll.cs341.web.form.EditNameForm;
import jakarta.persistence.ManyToOne;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class EditAccountController {

    UserService userService;

    public EditAccountController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/editAccount")
    public String editAccountGet(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        // Make the username available to the view for subsequent form submissions.
        model.addAttribute("user", user);
        model.addAttribute("editNameForm", new EditNameForm());

        return "editAccount";
    }

    @PostMapping("/editAccount")
    public String editAccountPost(HttpSession session, Model model, @Valid @ModelAttribute EditNameForm editNameForm, BindingResult bindingResult) {
        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        if(bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            return "editAccount";
        }

        userService.renameUser(user, editNameForm.getName());

        return "/editAccount";
    }
}
