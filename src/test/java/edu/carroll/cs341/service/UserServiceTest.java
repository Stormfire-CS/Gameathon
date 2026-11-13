package edu.carroll.cs341.service;

import edu.carroll.cs341.jpa.model.User;
import edu.carroll.cs341.jpa.repo.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.springframework.test.util.AssertionErrors.*;


/**
 * Tests the {@link UserService} methods responsible for validating login
 * credentials and checking whether new users can be registered.
 *
 * <p>Each test begins by clearing the user repository, verifying that the
 * test user does not already exist, and saving a user with known credentials.
 * The tests then verify the expected results for valid and invalid inputs,
 * including null values, incorrect passwords, existing usernames, and
 * mismatched passwords.</p>
 *
 * <p>The {@link SpringBootTest} annotation loads the Spring application
 * context so that the service and repository can be tested using their
 * configured dependencies. The {@link Transactional} annotation allows
 * database operations performed during each test to participate in a
 * transaction that is normally rolled back after the test completes.</p>
 *
 * <p>ChatGPT assisted with Javadoc documentation; code reviewed by author.
 * No code generated.</p>
 *
 * @author Max Montowski
 */
@SpringBootTest
@Transactional
public class UserServiceTest {
    private static final String username = "testuser";
    private static final String password = "testpass";

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepo;

    /*
     * = = = = = = = = = = =
     * Validate User Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that a user can be validated when the username and password
     * match the credentials stored in the database.
     */
    @Test
    public void validateUserSuccessTest() {
        insertTestingUser();
        User result = userService.validateUser(username, password);
        assertNotNull("validateUserSuccessTest: should succeed using the same user/pass info", result);
        assertEquals("validateUserSuccessTest: username of user who logged in should still be the same.",result.getUsername(),username);
    }

    /**
     * Verifies that a user can be validated when the username and password
     * match the credentials stored in the database and the username has a space at the end of it.
     */
    @Test
    public void validateUserSuccessTestWithSpaceAtEnd() {
        insertTestingUser();
        User result = userService.validateUser(username + " ", password);
        assertNotNull("validateUserSuccessTest: should succeed using the same user/pass info", result);
        assertEquals("validateUserSuccessTest: username of user who logged in should still be the same.",result.getUsername(),username);
    }

    /**
     * Verifies that a user can be validated when the username and password
     * match the credentials stored in the database and the username has a space at the beginning of it.
     */
    @Test
    public void validateUserSuccessTestWithSpaceAtStart() {
        insertTestingUser();
        User result = userService.validateUser(" " + username, password);
        assertNotNull("validateUserSuccessTest: should succeed using the same user/pass info", result);
        assertEquals("validateUserSuccessTest: username of user who logged in should still be the same.",result.getUsername(),username);
    }

    /**
     * Verifies that user validation fails when an existing username is
     * supplied with an incorrect password.
     */
    @Test
    public void validateUserExistingUserInvalidPasswordTest() {
        insertTestingUser();
        User result = userService.validateUser(username, password + "extra");
        assertNull("validateUserExistingUserInvalidPasswordTest: should fail using a valid user, invalid pass", result);
    }

    /**
     * Verifies that user validation fails when a nonexistent username is
     * supplied with an otherwise valid password.
     */
    @Test
    public void validateUserInvalidUserValidPasswordTest() {
        insertTestingUser();
        User result = userService.validateUser(username + "not", password);
        assertNull("validateUserInvalidUserValidPasswordTest: should fail using an invalid user, valid pass", result);
    }

    /**
     * Verifies that user validation fails when both the username and password
     * are invalid.
     */
    @Test
    public void validateUserInvalidUserInvalidPasswordTest() {
        insertTestingUser();
        User result = userService.validateUser(username + "not", password + "extra");
        assertNull("validateUserInvalidUserInvalidPasswordTest: should fail using an invalid user, valid pass", result);
    }

    /**
     * Verifies that user validation fails when the username is null and the
     * password is valid.
     */
    @Test
    public void validateUserNullUserValidPasswordTest() {
        insertTestingUser();
        User result = userService.validateUser(null, password);
        assertNull("validateUserNullUserInvalidPasswordTest: should fail using a null user, valid pass", result);
    }

    /**
     * Verifies that user validation fails when the username is valid but the
     * password is null.
     */
    @Test
    public void validateUserValidUserNullPasswordTest() {
        insertTestingUser();
        User result = userService.validateUser(username, null);
        assertNull("validateUserValidUserNullPasswordTest: should fail using a valid user, null pass", result);
    }

    /**
     * Verifies that user validation fails when the username is null and the
     * password is incorrect.
     */
    @Test
    public void validateUserNullUserInvalidPasswordTest() {
        insertTestingUser();
        User result = userService.validateUser(null, password + "extra");
        assertNull("validateUserNullUserInvalidPasswordTest: should fail using a null user, invalid pass", result);
    }

    /**
     * Verifies that user validation fails when the username does not exist
     * and the password is null.
     */
    @Test
    public void validateUserInvalidUserNullPasswordTest() {
        insertTestingUser();
        User result = userService.validateUser(username + "not", null);
        assertNull("validateUserInvalidUserNullPasswordTest: should fail using a invalid user, null pass", result);
    }

    /**
     * Verifies that user validation fails when both the username and password
     * are null.
     */
    @Test
    public void validateUserNullUserNullPasswordTest() {
        insertTestingUser();
        User result = userService.validateUser(null, null);
        assertNull("validateUserNullUserNullPasswordTest: should fail using a null user, null pass", result);
    }

    /*
     * = = = = = = = = = = =
     * New User Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that new-user validation succeeds when the username is
     * available and the two proposed passwords match.
     */
    @Test
    public void NewUserMatchingPasswordsTest() {
        insertTestingUser();
        String newUser = "DifferentTestUser";
        User result = userService.newUser(newUser, password, password);
        assertNotNull("NewUserMatchingPasswordsTest: should work using valid user, matching passwords", result);
        assertEquals("NewUserMatchingPasswordsTest: the new user should have the same username as the username used to create it"
                , result.getUsername(), newUser);
        List<User> insertedUsers = userRepo.findByUsernameIgnoreCase(username);
        assertTrue("NewUserMatchingPasswordsTest: should work, but not overwrite existing users", !insertedUsers.isEmpty());
        User existingUser = insertedUsers.getFirst();
        assertNotEquals("NewUserMatchingPasswordsTest: making a new user should not change an existing user", existingUser, result);

    }

    /**
     * Verifies that new-user validation fails when the requested username
     * already exists, even when the two proposed passwords match.
     */
    @Test
    public void newUserExistingUserPasswordsMatchTest() {
        insertTestingUser();
        String newPass = "NotTestPass";
        User result = userService.newUser(username, newPass, newPass);
        assertNull("newUserExistingUserPasswordsMatchTest: should fail using an existing user, matching passwords", result);
    }

    /**
     * Verifies that new-user validation fails when the requested username is
     * null, even when the two proposed passwords match.
     */
    @Test
    public void newUserNullUserPasswordsMatchTest() {
        insertTestingUser();
        String newPass = "NotTestPass";
        User result = userService.newUser(null, newPass, newPass);
        assertNull("newUserNullUserPasswordsMatchTest: should fail using an null user, matching passwords", result);
    }

    /**
     * Verifies that new-user validation fails when an existing username is
     * supplied with matching passwords equal to the stored user's password.
     */
    @Test
    public void newUserExistingUserExistingUserPasswordTest() {
        insertTestingUser();
        User result = userService.newUser(username, password, password);
        assertNull("newUserExistingUserExistingUserPasswordTest: should fail using an existing user, existing users matching passwords", result);

    }

    /**
     * Verifies that new-user validation fails when the requested username is
     * null and both proposed passwords match the stored user's password.
     */
    @Test
    public void newUserNullUserExistingUserPasswordsTest() {
        insertTestingUser();
        User result = userService.newUser(null, password, password);
        assertNull("newUserNullUserExistingUserPasswordsTest: should fail using a null user, existing users matching passwords", result);
    }

    /**
     * Verifies that new-user validation fails when one proposed password is
     * null and the other is valid, even when the username is available.
     */
    @Test
    public void newUserValidUserWithOneNullPasswordTest() {
        insertTestingUser();
        String newUser = "DifferentTestUser";
        User result = userService.newUser(newUser, null, password);
        assertNull("newUserValidUserWithOneNullPasswordTest: should fail using a valid user, null password, valid password", result);

    }

    /**
     * Verifies that new-user validation fails when an existing username is
     * supplied with one null password and one valid password.
     */
    @Test
    public void newUserExistingUserWithOneNullPasswordTest2() {
        insertTestingUser();
        User result = userService.newUser(username, null, password);
        assertNull("newUserExistingUserWithOneNullPasswordTest2: should fail using an existing user, null password, valid password", result);
    }

    /**
     * Verifies that new-user validation fails when the username and one
     * proposed password are null while the other password is valid.
     */
    @Test
    public void newUserNullUserNullPasswordValidPasswordTest() {
        User result = userService.newUser(null, null, password);
        assertNull("newUserNullUserNullPasswordValidPasswordTest: should fail using a null user, null password, valid password", result);
    }

    /**
     * Verifies that new-user validation fails when the username and the
     * confirmation password are null while the first password is valid.
     */
    @Test
    public void newUserValidUserValidPasswordNullPasswordTest() {
        String newUser = "DifferentTestUser";
        User result = userService.newUser(newUser, password, null);
        assertNull("newUserValidUserValidPasswordNullPasswordTest: should fail using a valid user, valid password, null password", result);
    }

    /**
     * Verifies that new-user validation fails when an existing username is
     * supplied with a valid password and a null confirmation password.
     */
    @Test
    public void newUserExistingUserValidPasswordNullPasswordTest() {
        insertTestingUser();
        User result = userService.newUser(username, password, null);
        assertNull("newUserExistingUserValidPasswordNullPasswordTest: should fail using an existing user, valid password, null password", result);
    }

    /**
     * Verifies that new-user validation fails when the username is null,
     * the first password is valid, and the confirmation password is null.
     */
    @Test
    public void newUserNullUserValidPasswordNullPasswordTest() {
        User result = userService.newUser(null, password, null);
        assertNull("newUserNullUserValidPasswordNullPasswordTest: should fail using a null user, valid password, null password", result);
    }

    /**
     * Verifies that new-user validation fails when the username is available
     * but both proposed passwords are null.
     */
    @Test
    public void newUserValidUserNullPasswordsTest() {
        String newUser = "DifferentTestUser";
        User result = userService.newUser(newUser, null, null);
        assertNull("newUserValidUserNullPasswordsTest: should fail using a valid user, null passwords", result);
    }

    /**
     * Verifies that new-user validation fails when an existing username is
     * supplied and both proposed passwords are null.
     */
    @Test
    public void newUserExistingUserNullPasswordsTest() {
        insertTestingUser();
        User result = userService.newUser(username, null, null);
        assertNull("newUserExistingUserNullPasswordsTest: should fail using an existing user, null passwords", result);
    }

    /**
     * Verifies that new-user validation fails when the username and both
     * proposed passwords are null.
     */
    @Test
    public void newUserNullUserNullPasswordsTest() {
        User result = userService.newUser(null, null, null);
        assertNull("newUserNullUserNullPasswordsTest: should fail using a null user, null passwords", result);
    }

    /**
     * Verifies that new-user validation fails when the username is available
     * but the two proposed passwords do not match.
     */
    @Test
    public void newUserValidUserNonMatchingPasswordsTest() {
        String newUser = "DifferentTestUser";
        String nonMatching1 = "123";
        String nonMatching2 = "abc";
        User result = userService.newUser(newUser, nonMatching1, nonMatching2);
        assertNull("newUserValidUserNonMatchingPasswordsTest: should fail using a valid user, non matching passwords", result);

    }

    /**
     * Verifies that new-user validation fails when the username already
     * exists and the two proposed passwords do not match.
     */
    @Test
    public void newUserExistingUserNonMatchingPasswordsTest() {
        insertTestingUser();
        String nonMatching1 = "123";
        String nonMatching2 = "abc";
        User result = userService.newUser(username, nonMatching1, nonMatching2);
        assertNull("newUserExistingUserNonMatchingPasswordsTest: should fail using an existing user, non matching passwords", result);
    }

    /**
     * Verifies that new-user validation fails when the username is null and
     * the two proposed passwords do not match.
     */
    @Test
    public void newUserNullUserNonMatchingPasswordsTest() {
        String nonMatching1 = "123";
        String nonMatching2 = "abc";
        User result = userService.newUser(null, nonMatching1, nonMatching2);
        assertNull("newUserNullUserNonMatchingPasswordsTest: should fail using a null user, non matching passwords", result);
    }

    /*
     * = = = = = = = = = = =
     * Rename User Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that a user's name can be successfully changed.
     */
    @Test
    public void renameUserSuccessTest() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        String newName = "NewTestName";
        boolean result = userService.renameUser(user, newName);
        assertTrue("renameUserSuccessTest: should successfully rename a valid user", result);
        assertEquals("renameUserSuccessTest: user's name should be updated",
                newName, user.getName());
    }

    /**
     * Verifies that renaming a user fails when the user is null.
     */
    @Test
    public void renameUserNullUserTest() {
        boolean result = userService.renameUser(null, "NewTestName");
        assertFalse("renameUserNullUserTest: should fail using a null user", result);
    }

    /**
     * Verifies that renaming a user fails when the new name is null.
     */
    @Test
    public void renameUserNullNameTest() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        boolean result = userService.renameUser(user, null);
        assertFalse("renameUserNullNameTest: should fail using a null name", result);
    }

    /**
     * Verifies that renaming a user fails when the new name is too short.
     */
    @Test
    public void renameUserShortNameTest() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        boolean result = userService.renameUser(user, "A");
        assertFalse("renameUserShortNameTest: should fail using a name shorter than 2 characters", result);
    }

    /**
     * Verifies that renaming a user fails when the new name is too long.
     */
    @Test
    public void renameUserLongNameTest() {
        insertTestingUser();
        User user = userService.validateUser(username, password);

        boolean result = userService.renameUser(user, "ThisNameIsMuchLongerThanTheAllowedFortyCharacters");
        assertFalse("renameUserLongNameTest: should fail using a name longer than 40 characters", result);
    }

    /*
     * = = = = = = = = = = =
     * Change Password Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that a user's password can be successfully changed.
     */
    @Test
    public void changeUsersPasswordSuccessTest() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        String newPassword = "NewTestPassword";

        boolean result = userService.changeUsersPassword(user, newPassword);

        assertTrue("changeUsersPasswordSuccessTest: should successfully change a valid user's password", result);
        assertNotNull("changeUsersPasswordSuccessTest: user should be able to log in with the new password",
                userService.validateUser(username, newPassword));
        assertNull("changeUsersPasswordSuccessTest: user should no longer be able to log in with the old password",
                userService.validateUser(username, password));
    }

    /**
     * Verifies that changing a password fails when the user is null.
     */
    @Test
    public void changeUsersPasswordNullUserTest() {
        boolean result = userService.changeUsersPassword(null, "NewTestPassword");
        assertFalse("changeUsersPasswordNullUserTest: should fail using a null user", result);
    }

    /**
     * Verifies that changing a password fails when the new password is null.
     */
    @Test
    public void changeUsersPasswordNullPasswordTest() {
        insertTestingUser();
        User user = userService.validateUser(username, password);

        boolean result = userService.changeUsersPassword(user, null);

        assertFalse("changeUsersPasswordNullPasswordTest: should fail using a null password", result);
        User checkUser = userService.validateUser(username, password);
        assertEquals("changeUsersPasswordNullPasswordTest: the users should still be able to log in with the old password", user, checkUser);

    }

    /**
     * Verifies that changing a password fails when the new password is too short.
     */
    @Test
    public void changeUsersPasswordShortPasswordTest() {
        insertTestingUser();
        User user = userService.validateUser(username, password);

        boolean result = userService.changeUsersPassword(user, "Short");

        assertFalse("changeUsersPasswordShortPasswordTest: should fail using a password shorter than 8 characters", result);
        User checkUser = userService.validateUser(username, password);
        assertEquals("changeUsersPasswordShortPasswordTest: the users should still be able to log in with the old password", user, checkUser);

    }

    /**
     * Verifies that changing a password fails when the new password is too long.
     */
    @Test
    public void changeUsersPasswordLongPasswordTest() {
        insertTestingUser();
        User user = userService.validateUser(username, password);

        boolean result = userService.changeUsersPassword(user,
                "ThisPasswordIsMuchLongerThanTheAllowedThirtyCharacters");
        assertFalse("changeUsersPasswordLongPasswordTest: should fail using a password longer than 30 characters", result);
        User checkUser = userService.validateUser(username, password);
        assertEquals("changeUsersPasswordLongPasswordTest: the users should still be able to log in with the old password", user, checkUser);

    }

    /**
     * Verifies that changing a password fails when the new password contains whitespace.
     */
    @Test
    public void changeUsersPasswordWhitespaceTest() {
        insertTestingUser();
        User user = userService.validateUser(username, password);
        boolean result = userService.changeUsersPassword(user, "New Test Password");
        assertFalse("changeUsersPasswordWhitespaceTest: should fail using a password containing whitespace", result);
        User checkUser = userService.validateUser(username, password);
        assertEquals("changeUsersPasswordWhitespaceTest: the users should still be able to log in with the old password", user, checkUser);
    }

    private void insertTestingUser() {
        userService.newUser(username, password, password);
    }
}