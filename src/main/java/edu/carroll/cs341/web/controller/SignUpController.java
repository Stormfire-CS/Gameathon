package edu.carroll.cs341.web.controller;

import edu.carroll.cs341.service.UserService;
import edu.carroll.cs341.web.form.CreateUserForm;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SignUpController {
    private final UserService userService;

    public SignUpController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/signUpPage")
    public String signUpGet(Model model) {
        model.addAttribute("createUserForm", new CreateUserForm());
        return "signUpPage";
    }

    @PostMapping("/signUpPage")
    public String signUpPost(@Valid @ModelAttribute CreateUserForm createUserForm, BindingResult result, RedirectAttributes attrs) {
        if (result.hasErrors()) {
            return "signUpPage";
        }

        if (!userService.validateNewUser(createUserForm.getUsername(), createUserForm.getPassword1(), createUserForm.getPassword2())){
            result.addError(new ObjectError("globalError", "Username already exists or passwords do not match"));
            return "signUpPage";
        }
        attrs.addAttribute("username", createUserForm.getUsername());
        return "redirect:/homePage";
    }
}
