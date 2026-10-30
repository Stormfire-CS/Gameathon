package edu.carroll.cs341.web.controller;

import edu.carroll.cs341.jpa.model.Family;
import edu.carroll.cs341.jpa.model.User;
import edu.carroll.cs341.service.FamilyService;
import edu.carroll.cs341.service.GamesOwnedService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Handles web requests for displaying a user's family information.
 * This controller retrieves the family associated with the logged-in user
 * and determines whether the user belongs to a family and administers it.
 * <p>
 * The controller adds the relevant information to the model so the
 * {@code familyPage} Thymeleaf view can display the appropriate content
 * based on the user's family membership and administrative status.
 *
 * <p><b>Sources Used:</b></p>
 * <ul>
 *   <li>ChatGPT – Assisted with reviewing and updating Javadoc comments for this file
 *   in conjunction with Max Montowski.
 *   All comments were reviewed by the author.</li>
 * </ul>
 */
@Controller
public class FamilyPageController {

    /**
     * Service used to retrieve family information and check administrator permissions.
     */
    private final FamilyService familyService;

    private final GamesOwnedService gamesOwnedService;

    /**
     * Constructs the controller with the services required to retrieve
     * family information and games owned by family members.
     *
     * @param familyService     service for family-related operations
     * @param gamesOwnedService service for retrieving family-owned games
     */
    public FamilyPageController(FamilyService familyService, GamesOwnedService gamesOwnedService) {
        this.familyService = familyService;
        this.gamesOwnedService = gamesOwnedService;
    }

    /**
     * Displays the family page for the logged-in user.
     * Retrieves the user's family, determines whether the user is a family
     * member, checks administrator status, and retrieves the family's
     * owned games when a family exists.
     *
     * @param session the HTTP session containing the logged-in user
     * @param model   the model used to supply data to the view
     * @return the family page view, or a redirect to the login page
     * if no user is logged in
     */
    @GetMapping("/familyPage")
    public String familyPage(HttpSession session, Model model) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        // Make the username available to the view for display and subsequent actions.
        model.addAttribute("user", user);

        // Retrieve the family associated with the user's accepted membership.
        Family family = familyService.getFamilyForUser(user);
        model.addAttribute("family", family);

        // A user is considered a family member when a family record is returned.
        model.addAttribute("isFamilyMember", family != null);

        // Only check administrator status when the user has an associated family.
        model.addAttribute("isFamilyAdmin", family != null && familyService.isFamilyAdmin(user, family));

        if (family != null) {
            model.addAttribute("games", gamesOwnedService.getGamesOwnedByFamily(family));
        }
        return "familyPage";
    }

    /**
     * Removes the logged-in user from their family and redirects to the family page.
     *
     * @param session the HTTP session containing the logged-in user
     * @return a redirect to the family page, or the login page
     * if no user is logged in
     */
    @PostMapping("/leaveFamily")
    public String leaveFamily(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        familyService.leaveFamily(user);

        return "redirect:/familyPage";
    }
}
