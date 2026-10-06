package edu.carroll.cs341.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

@Controller
public class SignUpController {

    @GetMapping("/signUpPage")
    public String signUpPage() {
        return "signUpPage";
    }
}
