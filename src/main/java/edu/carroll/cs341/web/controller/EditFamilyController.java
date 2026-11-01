package edu.carroll.cs341.web.controller;

import edu.carroll.cs341.jpa.model.FamilyMembership;
import edu.carroll.cs341.jpa.model.User;
import edu.carroll.cs341.service.FamilyMembershipService;
import edu.carroll.cs341.service.FamilyService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Handles web requests for managing family membership and family settings.
 * This controller displays family membership information and pending requests
 * and processes requests to accept, reject, or remove family members.
 * It also handles renaming a family.
 * <p>
 * The controller delegates membership-related operations to
 * {@link FamilyService} and supplies the information required by the
 * corresponding Thymeleaf view.
 *
 * <p><b>Sources Used:</b></p>
 * <ul>
 *   <li>ChatGPT – Assisted with reviewing and updating Javadoc comments for this file
 *   in conjunction with Max Montowski.
 *   All comments were reviewed by the author.</li>
 * </ul>
 */
@Controller
public class EditFamilyController {

    /**
     * Service used to retrieve and manage family requests.
     */
    private final FamilyService familyService;

    /**
     * Service used to retrieve and manage family membership requests.
     */
    private final FamilyMembershipService familyMembershipService;

    /**
     * Constructs the controller with the service required to manage
     * family membership requests.
     *
     * @param familyService service for family and membership operations
     */
    public EditFamilyController(FamilyService familyService, FamilyMembershipService familyMembershipService) {
        this.familyService = familyService;
        this.familyMembershipService = familyMembershipService;
    }

    /**
     * Displays the page containing family membership information and pending requests.
     * Adds the logged-in user, family members, family information, and pending
     * requests to the model for use by the view.
     *
     * @param session the HTTP session containing the logged-in user
     * @param model   the model used to supply data to the view
     * @return the family management view, or a redirect to the login page
     * if no user is logged in
     */
    @GetMapping("/editFamilyPage")
    public String addFamilyPage(HttpSession session, Model model) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        // Make the username available to the view for subsequent actions.
        model.addAttribute("user", user);

        model.addAttribute("members", familyMembershipService.getFamilyMembers(user));
        model.addAttribute("family", familyMembershipService.getFamilyForUser(user));

        // Retrieve pending requests so the administrator can review them.
        model.addAttribute("requests", familyMembershipService.getPendingRequests(user));
        return "editFamily";
    }

    /**
     * Accepts a pending family membership request and redirects back to
     * the family membership management page.
     *
     * @param session      the HTTP session containing the logged-in user
     * @param membershipID the ID of the membership request to accept
     * @return a redirect to the family management page, or a redirect to
     * the family page if the membership record cannot be found
     */
    @PostMapping("/acceptFamilyMember")
    public String acceptFamilyMember(HttpSession session, Long membershipID) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        FamilyMembership membership = familyMembershipService.getMembershipByID(membershipID);

        if (membership == null) {
            return "redirect:/addFamilyPage";
        }

        // Delegate request validation and acceptance to the service layer.
        familyMembershipService.acceptRequest(user, membership);

        // Return to the request list so it can be refreshed.
        return "redirect:/editFamilyPage";
    }

    /**
     * Rejects a pending family membership request and redirects back to
     * the family membership management page.
     *
     * @param session      the HTTP session containing the logged-in user
     * @param membershipID the ID of the membership request to reject
     * @return a redirect to the family management page, or a redirect to
     * the family page if the membership record cannot be found
     */
    @PostMapping("/rejectFamilyMember")
    public String rejectFamilyMember(HttpSession session, Long membershipID) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        FamilyMembership membership = familyMembershipService.getMembershipByID(membershipID);

        if (membership == null) {
            return "redirect:/addFamilyPage";
        }

        // Delegate request validation and rejection to the service layer.
        familyMembershipService.rejectRequest(user, membership);

        // Return to the request list so it can be refreshed.
        return "redirect:/editFamilyPage";
    }

    /**
     * Renames the family associated with the logged-in user.
     *
     * @param session    the HTTP session containing the logged-in user
     * @param familyName the new name for the family
     * @return a redirect to the family management page, or the login page
     * if no user is logged in
     */
    @PostMapping("/renameFamily")
    public String renameFamily(HttpSession session, String familyName) {
        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        familyService.renameFamily(user, familyName);

        return "redirect:/editFamilyPage";
    }

    /**
     * Removes a member from the family associated with the logged-in user.
     *
     * @param session      the HTTP session containing the logged-in user
     * @param membershipID the ID of the family membership record to remove
     * @return a redirect to the family management page, or the login page
     * if no user is logged in
     */
    @PostMapping("/removeFamilyMember")
    public String removeFamilyMember(HttpSession session, Long membershipID) {
        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        familyMembershipService.removeFamilyMember(user, membershipID);

        return "redirect:/editFamilyPage";
    }
}
