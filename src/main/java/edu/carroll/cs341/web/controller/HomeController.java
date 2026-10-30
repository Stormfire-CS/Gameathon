package edu.carroll.cs341.web.controller;

import edu.carroll.cs341.jpa.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Handles web requests for displaying the application's home page.
 * This controller makes the logged-in user's information available to the view
 * so the page can display user-specific information and provide navigation
 * to other parts of the application.
 *
 * <p><b>Sources Used:</b></p>
 * <ul>
 *   <li>ChatGPT – Assisted with reviewing and updating Javadoc comments for this file
 *   in conjunction with Max Montowski.
 *   All comments were reviewed by the author.</li>
 * </ul>
 */
@Controller
public class HomeController {

    /**
     * Displays the application's home page.
     * Adds the logged-in user to the model so the view can access their information.
     *
     * @param session the HTTP session containing the logged-in user
     * @param model   the model used to supply data to the view
     * @return the home page view, or a redirect to the login page
     * if no user is logged in
     */
    @GetMapping("/homePage")
    public String homePage(HttpSession session, Model model) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        // Make the username available to the view for display and navigation.
        model.addAttribute("user", user);

        return "homePage";
    }

}
