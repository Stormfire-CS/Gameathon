package edu.carroll.cs341.web.controller;

import edu.carroll.cs341.jpa.model.User;
import edu.carroll.cs341.service.UserService;
import edu.carroll.cs341.web.form.CreateUserForm;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Handles web requests related to creating new user accounts.
 * Displays the sign-up page, validates submitted registration information,
 * and redirects users to the home page when registration is successful.
 *
 * <p>The controller delegates user creation to {@link UserService} and uses
 * Spring validation to handle errors in the submitted registration form.</p>
 *
 * <p><b>Sources Used:</b></p>
 * <ul>
 *   <li>ChatGPT – Assisted with reviewing and updating Javadoc comments for this file
 *   in conjunction with Max Montowski.
 *   All comments were reviewed by the author.</li>
 * </ul>
 */
@Controller
public class SignUpController {

    /**
     * Service used to validate new user registration information.
     */
    private final UserService userService;

    /**
     * Constructs the sign-up controller with the service required to
     * handle user registration.
     *
     * @param userService service used for user-related operations
     */
    public SignUpController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Displays the sign-up page and adds an empty user creation form
     * to the model.
     *
     * @param model the model used to provide attributes to the view
     * @return the name of the sign-up page view
     */
    @GetMapping("/signUpPage")
    public String signUpGet(Model model) {
        // Provide a new form object for the user to complete.
        model.addAttribute("createUserForm", new CreateUserForm());

        return "signUpPage";
    }

    /**
     * Processes a submitted registration form.
     * Returns the sign-up page if field validation fails, the username
     * already exists, or the passwords do not match. Otherwise, creates
     * the user's session and redirects to the home page.
     *
     * @param createUserForm the submitted form containing the username
     *                       and both password entries
     * @param result         the result of validating the submitted form
     * @param session        the HTTP session used to store the logged-in user
     *                       and configure the session inactivity timeout
     * @return the sign-up view when validation fails, or a redirect
     * to the home page when registration succeeds
     */
    @PostMapping("/signUpPage")
    public String signUpPost(@Valid @ModelAttribute CreateUserForm createUserForm, BindingResult result, HttpSession session) {

        // Return to the form if any field-level validation rules fail.
        if (result.hasErrors()) {
            return "signUpPage";
        }

        User user = userService.newUser(createUserForm.getUsername(), createUserForm.getPassword1(), createUserForm.getPassword2());

        // Check whether the username is available and the passwords match.
        if (user == null) {

            // Add a form-level error when registration details are invalid.
            result.addError(new ObjectError("globalError", "Username already exists or passwords do not match"));
            return "signUpPage";
        }

        // Set the session to expire after 15 minutes of inactivity.
        session.setMaxInactiveInterval(10 * 60);

        // Store the logged-in User in the session.
        session.setAttribute("loggedInUser", user);

        return "redirect:/homePage";
    }
}
