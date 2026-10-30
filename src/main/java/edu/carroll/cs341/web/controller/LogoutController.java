package edu.carroll.cs341.web.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Handles requests related to logging out of the application.
 * This controller invalidates the current user's HTTP session and redirects
 * the user to the login page.
 *
 * <p><b>Sources Used:</b></p>
 * <ul>
 *   <li>ChatGPT – Assisted with reviewing and updating Javadoc comments for this file
 *   in conjunction with Max Montowski.
 *   All comments were reviewed by the author.</li>
 * </ul>
 */
@Controller
public class LogoutController {

    /**
     * Logs out the current user by invalidating their HTTP session.
     *
     * @param session the HTTP session associated with the current user
     * @return a redirect to the login page
     */
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        // End the current user's session.
        session.invalidate();

        // Return to the login page.
        return "redirect:/login";
    }
}