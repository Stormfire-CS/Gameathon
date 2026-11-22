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

    @Autowired
    private UserService userService;

    /*
     * = = = = = = = = = = =
     * Validate User Tests
     * = = = = = = = = = = =
     */


    //Also test usersnames with all caps, that still work, or a username that is entered with all lower case, that works when you enter all caps,
    // Try entering mix case, try setting the username as mixed case and then verify entering different cases works, try setting it as all caps, and then veryify works with different cases.
    /**
     * Verifies that a user can be validated when the username and password
     * match the credentials stored in the database, when the database is empty, has one user, or has two users.
     */
    @Test
    public void validateUserSuccessTest() {
        // No users in the database
        final String username = "testUser";
        final String password = "testpass";
        final String username2 = "testUser2";
        final String password2 = "testpass2";
        User nullUser = userService.validateUser(username, password);
        assertNull("validateUserSuccessTest: user should be null when there is no user in the database", nullUser);
        // One user in the database
        User checkUser = insertTestingUser(username, password);
        User sucsefulUser1 = userService.validateUser(username, password);
        assertNotNull("validateUserSuccessTest: should succeed using the same user/pass info", sucsefulUser1);
        assertEquals("validateUserSuccessTest: user who logged in should still be the same user that was created.", sucsefulUser1, checkUser);
        // Two users in the database
        User checkUser2 = insertTestingUser(username2, password2);
        User sucsesfulUser2 = userService.validateUser(username2, password2);
        assertNotNull("validateUserSuccessTest: should succeed using the same user/pass info", sucsesfulUser2);
        assertEquals("validateUserSuccessTest: user who logged in should still be the same user that was created.", sucsesfulUser2, checkUser2);
        User sucsesfulUser3 = userService.validateUser(username, password);
        assertNotNull("validateUserSuccessTest: should succeed using the same user/pass info", sucsesfulUser3);
        assertEquals("validateUserSuccessTest: user of user who logged in should still be the same after another user was added.", sucsesfulUser3, checkUser);
        assertNotEquals("validateUserSuccessTest: users should not be the same.", sucsesfulUser2, sucsesfulUser3);
    }

    /**
     * Verifies that a user can be validated when the username and password
     * match the credentials stored in the database and the username has a space at the end, beginning, or both.
     * Checks these conditions for an empty database, a database with one user, and a database with two users.
     */
    @Test
    public void validateUserSuccessTestWithSpaces() {
        // No user in the database
        final String username = "testUser";
        final String password = "testpass";
        final String username2 = "testUser2";
        final String password2 = "testpass2";
        User nullUser1 = userService.validateUser(username + " ", password);
        User nullUser2 = userService.validateUser(" " + username, password);
        User nullUser3 = userService.validateUser(" " + username + " ", password);
        assertNull("validateUserSuccessTest: should fail using a valid user with a space because no user is in the database", nullUser1);
        assertNull("validateUserSuccessTest: should fail using a valid user with a space because no user is in the database", nullUser2);
        assertNull("validateUserSuccessTest: should fail using a valid user with a space because no user is in the database", nullUser3);

        // One user in the database
        User checkUser = insertTestingUser(username, password);
        User sucsesfulUser1 = userService.validateUser(username + " ", password);
        User sucsesfulUser2 = userService.validateUser(" " + username, password);
        User sucsesfulUser3 = userService.validateUser(" " + username + " ", password);
        assertNotNull("validateUserSuccessTest: should succeed when using a valid user with a space at the end", sucsesfulUser1);
        assertNotNull("validateUserSuccessTest: should succeed when using a valid user with a space at the beginning", sucsesfulUser2);
        assertNotNull("validateUserSuccessTest: should succeed when using a valid user with a space at the beginning and end", sucsesfulUser3);
        assertEquals("validateUserSuccessTest: user who logged in should still be the same after a space was added to the end of the login.", sucsesfulUser1, checkUser);
        assertEquals("validateUserSuccessTest: user who logged in should still be the same after a space was added to the beginning of the login.", sucsesfulUser2, checkUser);
        assertEquals("validateUserSuccessTest: user who logged in should still be the same after a space was added to the beginning and end of the login.", sucsesfulUser3, checkUser);

        // Two users in the database
        User checkUser2 = insertTestingUser(username2, password2);
        sucsesfulUser1 = userService.validateUser(username + " ", password);
        sucsesfulUser2 = userService.validateUser(" " + username, password);
        sucsesfulUser3 = userService.validateUser(" " + username + " ", password);
        User sucsesfulUser4 = userService.validateUser(username2 + " ", password2);
        User sucsesfulUser5 = userService.validateUser(" " + username2, password2);
        User sucsesfulUser6 = userService.validateUser(" " + username2 + " ", password2);
        assertNotNull("validateUserSuccessTest: should succeed using an existing valid user with a space at the end", sucsesfulUser1);
        assertNotNull("validateUserSuccessTest: should succeed using an existing valid user with a space at the beginning", sucsesfulUser2);
        assertNotNull("validateUserSuccessTest: should succeed using an existing valid user with a space at the beginning and end", sucsesfulUser3);
        assertNotNull("validateUserSuccessTest: should succeed using the new valid user with a space at the end", sucsesfulUser4);
        assertNotNull("validateUserSuccessTest: should succeed using the new valid user with a space at the beginning", sucsesfulUser5);
        assertNotNull("validateUserSuccessTest: should succeed using the new valid user with a space at the beginning and end", sucsesfulUser6);
        assertEquals("validateUserSuccessTest: user who logged in should still be the same after a space was added to the end of the login, and a second user was added", sucsesfulUser1, checkUser);
        assertEquals("validateUserSuccessTest: user who logged in should still be the same after a space was added to the beginning of the login, and a second user was added", sucsesfulUser2, checkUser);
        assertEquals("validateUserSuccessTest: user who logged in should still be the same after a space was added to the beginning and end of the login, and a second user was added", sucsesfulUser3, checkUser);
        assertEquals("validateUserSuccessTest: second user who logged in should still be the same after a space was added to the end of the login", sucsesfulUser4, checkUser2);
        assertEquals("validateUserSuccessTest: second user who logged in should still be the same after a space was added to the beginning of the login", sucsesfulUser5, checkUser2);
        assertEquals("validateUserSuccessTest: second user who logged in should still be the same after a space was added to the beginning and end of the login", sucsesfulUser6, checkUser2);
    }

    /**
     * Verifies that username casing does not affect login for accounts
     * registered with lowercase, uppercase, or mixed-case usernames.
     */
    @Test
    public void validateUserSuccessTestWithDifferentCases() {
        final String lowercaseUsername = "lowercaseuser";
        final String lowercasePassword = "testpass1";
        final String uppercaseUsername = "UPPERCASEUSER";
        final String uppercasePassword = "testpass2";
        final String mixedCaseUsername = "MixedCaseUser";
        final String mixedCasePassword = "testpass3";

        // Lowercase username entered in uppercase and mixed case.
        User lowercaseUser = insertTestingUser(lowercaseUsername, lowercasePassword);
        User result = userService.validateUser("LOWERCASEUSER", lowercasePassword);
        assertEquals("validateUserSuccessTestWithDifferentCases: uppercase input should find the lowercase username", lowercaseUser, result);
        result = userService.validateUser("LowerCaseUser", lowercasePassword);
        assertEquals("validateUserSuccessTestWithDifferentCases: mixed-case input should find the lowercase username", lowercaseUser, result);

        // Uppercase username entered in lowercase and mixed case.
        User uppercaseUser = insertTestingUser(uppercaseUsername, uppercasePassword);
        result = userService.validateUser("uppercaseuser", uppercasePassword);
        assertEquals("validateUserSuccessTestWithDifferentCases: lowercase input should find the uppercase username", uppercaseUser, result);
        result = userService.validateUser("UpperCaseUser", uppercasePassword);
        assertEquals("validateUserSuccessTestWithDifferentCases: mixed-case input should find the uppercase username", uppercaseUser, result);

        // Mixed-case username entered in lowercase, uppercase, and a different mixed case.
        User mixedCaseUser = insertTestingUser(mixedCaseUsername, mixedCasePassword);
        result = userService.validateUser("mixedcaseuser", mixedCasePassword);
        assertEquals("validateUserSuccessTestWithDifferentCases: lowercase input should find the mixed-case username", mixedCaseUser, result);
        result = userService.validateUser("MIXEDCASEUSER", mixedCasePassword);
        assertEquals("validateUserSuccessTestWithDifferentCases: uppercase input should find the mixed-case username", mixedCaseUser, result);
        result = userService.validateUser("mIXEDcASEuSER", mixedCasePassword);
        assertEquals("validateUserSuccessTestWithDifferentCases: different mixed-case input should find the mixed-case username", mixedCaseUser, result);
    }

    /**
     * Verifies that user validation succeeds when two users have the same password.
     */
    @Test
    public void validateUserTwoUsersWithSamePassword() {
        final String username = "testUser";
        final String password = "testPassword";
        final String username2 = "testUser2";

        User checkUser = insertTestingUser(username, password);
        User checkUser2 = insertTestingUser(username2, password);
        User sucsefulUser1 = userService.validateUser(username, password);
        User sucsefulUser2 = userService.validateUser(username2, password);

        assertNotNull("validateUserTwoUsersWithSamePassword: should succeed using a valid user, valid pass", sucsefulUser1);
        assertNotNull("validateUserTwoUsersWithSamePassword: should succeed using a valid user, same valid pass as first user", sucsefulUser2);
        assertEquals("validateUserTwoUsersWithSamePassword: user should be still be the same after logging in and a second user is added", checkUser, sucsefulUser1);
        assertEquals("validateUserTwoUsersWithSamePassword: user should still be the same after logging in ", checkUser2, sucsefulUser2);
        assertNotEquals("validateUserTwoUsersWithSamePassword: users should not be equal after logging in", sucsefulUser1, sucsefulUser2);
    }

    /**
     * Verifies that incorrect passwords fail with one or two stored users.
     * Checks missing and extra characters at the beginning, end, or both.
     */
    @Test
    public void validateUserExistingUserInvalidPasswordTest() {
        final String username = "testUser";
        final String password = "testPassword";
        final String username2 = "testUser2";
        final String password2 = "testPassword2";

        // One user in the database
        User checkUser = insertTestingUser(username, password);

        // Incorect password
        User failedLogin1 = userService.validateUser(username, "incorrectPassword");
        assertNull("validateUserExistingUserInvalidPasswordTest: should reject an incorrect password", failedLogin1);

        // Missing character'(s) in password
        User failedLogin2 = userService.validateUser(username, password.substring(1));
        assertNull("validateUserExistingUserInvalidPasswordTest: should reject a missing first character", failedLogin2);
        User failedLogin3 = userService.validateUser(username, password.substring(0, password.length() - 1));
        assertNull("validateUserExistingUserInvalidPasswordTest: should reject a missing last character", failedLogin3);
        User failedLogin4 = userService.validateUser(username, password.substring(1, password.length() - 1));
        assertNull("validateUserExistingUserInvalidPasswordTest: should reject missing first and last characters", failedLogin4);

        // Extra character'(s) in password
        User failedLogin5 = userService.validateUser(username, "x" + password);
        assertNull("validateUserExistingUserInvalidPasswordTest: should reject an extra first character", failedLogin5);
        User failedLogin6 = userService.validateUser(username, password + "x");
        assertNull("validateUserExistingUserInvalidPasswordTest: should reject an extra last character", failedLogin6);
        User failedLogin7 = userService.validateUser(username, "x" + password + "x");
        assertNull("validateUserExistingUserInvalidPasswordTest: should reject extra first and last characters", failedLogin7);

        // Null user
        User failedLoginOneUser = userService.validateUser(username, null);
        assertNull("validateUserExistingUserInvalidPasswordTest: should reject a null password with one user", failedLoginOneUser);

        // Spaces in password
        failedLoginOneUser = userService.validateUser(username, password + " ");
        assertNull("validateUserExistingUserInvalidPasswordTest: should reject a trailing space with one user", failedLoginOneUser);
        failedLoginOneUser = userService.validateUser(username, " " + password);
        assertNull("validateUserExistingUserInvalidPasswordTest: should reject a leading space with one user", failedLoginOneUser);
        failedLoginOneUser = userService.validateUser(username, " " + password + " ");
        assertNull("validateUserExistingUserInvalidPasswordTest: should reject leading and trailing spaces with one user", failedLoginOneUser);

        // Passwords with only spaces
        failedLoginOneUser = userService.validateUser(username, "        ");
        assertNull("validateUserExistingUserInvalidPasswordTest: should reject an all-spaces password with one user", failedLoginOneUser);

        // Empty password
        failedLoginOneUser = userService.validateUser(username, "");
        assertNull("validateUserExistingUserInvalidPasswordTest: should reject an empty password with one user", failedLoginOneUser);

        // User has not been changed by failing to login.
        User stillSame = userService.validateUser(username, password);
        assertNotNull("validateUserExistingUserInvalidPasswordTest: should still return a valid user", stillSame);
        assertEquals("validateUserExistingUserInvalidPasswordTest: should still return the same user as originally created", stillSame, checkUser);

        // Two users in the database
        User checkUser2 = insertTestingUser(username2, password2);
        User failedLogin8 = userService.validateUser(username, "incorrectPassword");
        assertNull("validateUserExistingUserInvalidPasswordTest: should still reject an incorrect password", failedLogin8);

        // Missing character'(s) in password for both users
        User failedLogin9 = userService.validateUser(username, password.substring(1));
        assertNull("validateUserExistingUserInvalidPasswordTest: should still reject a missing first character", failedLogin9);
        User failedLogin10 = userService.validateUser(username, password.substring(0, password.length() - 1));
        assertNull("validateUserExistingUserInvalidPasswordTest: should still reject a missing last character", failedLogin10);
        User failedLogin11 = userService.validateUser(username, password.substring(1, password.length() - 1));
        assertNull("validateUserExistingUserInvalidPasswordTest: should still reject missing first and last characters", failedLogin11);
        User failedLogin12 = userService.validateUser(username2, password2.substring(1));
        assertNull("validateUserExistingUserInvalidPasswordTest: should still reject a missing first character", failedLogin12);
        User failedLogin13 = userService.validateUser(username2, password2.substring(0, password2.length() - 1));
        assertNull("validateUserExistingUserInvalidPasswordTest: should still reject a missing last character", failedLogin13);
        User failedLogin14 = userService.validateUser(username2, password2.substring(1, password2.length() - 1));
        assertNull("validateUserExistingUserInvalidPasswordTest: should still reject a missing first and last character", failedLogin14);

        // Extra character'(s) in password for both users
        User failedLogin15 = userService.validateUser(username, "x" + password);
        assertNull("validateUserExistingUserInvalidPasswordTest: should still reject an extra first character", failedLogin15);
        User failedLogin16 = userService.validateUser(username, password + "x");
        assertNull("validateUserExistingUserInvalidPasswordTest: should still reject an extra last character", failedLogin16);
        User failedLogin17 = userService.validateUser(username, "x" + password + "x");
        assertNull("validateUserExistingUserInvalidPasswordTest: should still reject extra first and last characters", failedLogin17);
        User failedLogin18 = userService.validateUser(username2, "x" + password);
        assertNull("validateUserExistingUserInvalidPasswordTest: should still reject an extra first character", failedLogin18);
        User failedLogin19 = userService.validateUser(username2, password + "x");
        assertNull("validateUserExistingUserInvalidPasswordTest: should still reject an extra last character", failedLogin19);
        User failedLogin20 = userService.validateUser(username2, "x" + password + "x");
        assertNull("validateUserExistingUserInvalidPasswordTest: should still reject extra first and last characters", failedLogin20);

        // Null, empty and whitespace passwords for the first user
        User failedLogin23 = userService.validateUser(username, null);
        assertNull("validateUserExistingUserInvalidPasswordTest: first user should reject a null password", failedLogin23);
        User failedLogin24 = userService.validateUser(username, password + " ");
        assertNull("validateUserExistingUserInvalidPasswordTest: first user should reject a trailing space", failedLogin24);
        User failedLogin25 = userService.validateUser(username, " " + password);
        assertNull("validateUserExistingUserInvalidPasswordTest: first user should reject a leading space", failedLogin25);
        User failedLogin26 = userService.validateUser(username, " " + password + " ");
        assertNull("validateUserExistingUserInvalidPasswordTest: first user should reject leading and trailing spaces", failedLogin26);
        User failedLogin27 = userService.validateUser(username, "        ");
        assertNull("validateUserExistingUserInvalidPasswordTest: first user should reject an all-spaces password", failedLogin27);
        User failedLogin28 = userService.validateUser(username, "");
        assertNull("validateUserExistingUserInvalidPasswordTest: first user should reject an empty password", failedLogin28);

        // Null and whitespace passwords for the second user
        User failedLogin29 = userService.validateUser(username2, null);
        assertNull("validateUserExistingUserInvalidPasswordTest: second user should reject a null password", failedLogin29);
        User failedLogin30 = userService.validateUser(username2, password2 + " ");
        assertNull("validateUserExistingUserInvalidPasswordTest: second user should reject a trailing space", failedLogin30);
        User failedLogin31 = userService.validateUser(username2, " " + password2);
        assertNull("validateUserExistingUserInvalidPasswordTest: second user should reject a leading space", failedLogin31);
        User failedLogin32 = userService.validateUser(username2, " " + password2 + " ");
        assertNull("validateUserExistingUserInvalidPasswordTest: second user should reject leading and trailing spaces", failedLogin32);
        User failedLogin33 = userService.validateUser(username2, "        ");
        assertNull("validateUserExistingUserInvalidPasswordTest: second user should reject an all-spaces password", failedLogin33);
        User failedLogin34 = userService.validateUser(username2, "");
        assertNull("validateUserExistingUserInvalidPasswordTest: second user should reject an empty password", failedLogin34);

        // Checking a valid password for one account does not give access to another account.
        User failedLogin21 = userService.validateUser(username, password2);
        assertNull("validateUserExistingUserInvalidPasswordTest: should reject the second user's password for the first user", failedLogin21);
        User failedLogin22 = userService.validateUser(username2, password);
        assertNull("validateUserExistingUserInvalidPasswordTest: should reject the first user's password for the second user", failedLogin22);

        stillSame = userService.validateUser(username, password);
        assertNotNull("validateUserExistingUserInvalidPasswordTest: first user should still be able to log in", stillSame);
        assertEquals("validateUserExistingUserInvalidPasswordTest: first user should still be the same after failed login attempts", checkUser, stillSame);
        User stillSame2 = userService.validateUser(username2, password2);
        assertNotNull("validateUserExistingUserInvalidPasswordTest: second user should still be able to log in", stillSame2);
        assertEquals("validateUserExistingUserInvalidPasswordTest: second user should still be the same after failed login attempts", checkUser2, stillSame2);
    }

    /**
     * Verifies that passwords must match the case used when the account was created.
     */
    @Test
    public void validateUserPasswordCaseSensitiveTest() {
        final String lowercaseUsername = "lowercasePasswordUser";
        final String lowercasePassword = "lowercasepass";
        final String uppercaseUsername = "uppercasePasswordUser";
        final String uppercasePassword = "UPPERCASEPASS";
        final String mixedCaseUsername = "mixedCasePasswordUser";
        final String mixedCasePassword = "MixedCasePass";

        User lowercaseUser = insertTestingUser(lowercaseUsername, lowercasePassword);
        User uppercaseUser = insertTestingUser(uppercaseUsername, uppercasePassword);
        User mixedCaseUser = insertTestingUser(mixedCaseUsername, mixedCasePassword);

        // Lowercase password entered in uppercase and mixed case.
        User result = userService.validateUser(lowercaseUsername, "LOWERCASEPASS");
        assertNull("validateUserPasswordCaseSensitiveTest: lowercase password should reject uppercase input", result);
        result = userService.validateUser(lowercaseUsername, "LowerCasePass");
        assertNull("validateUserPasswordCaseSensitiveTest: lowercase password should reject mixed-case input", result);

        // Uppercase password entered in lowercase and mixed case.
        result = userService.validateUser(uppercaseUsername, "uppercasepass");
        assertNull("validateUserPasswordCaseSensitiveTest: uppercase password should reject lowercase input", result);
        result = userService.validateUser(uppercaseUsername, "UpperCasePass");
        assertNull("validateUserPasswordCaseSensitiveTest: uppercase password should reject mixed-case input", result);

        // Mixed-case password entered in lowercase, uppercase, and a different mixed case.
        result = userService.validateUser(mixedCaseUsername, "mixedcasepass");
        assertNull("validateUserPasswordCaseSensitiveTest: mixed-case password should reject lowercase input", result);
        result = userService.validateUser(mixedCaseUsername, "MIXEDCASEPASS");
        assertNull("validateUserPasswordCaseSensitiveTest: mixed-case password should reject uppercase input", result);
        result = userService.validateUser(mixedCaseUsername, "mixedCasePass");
        assertNull("validateUserPasswordCaseSensitiveTest: mixed-case password should reject a single case change", result);

        // Each account should still accept its original password.
        result = userService.validateUser(lowercaseUsername, lowercasePassword);
        assertEquals("validateUserPasswordCaseSensitiveTest: original lowercase password should still work", lowercaseUser, result);
        result = userService.validateUser(uppercaseUsername, uppercasePassword);
        assertEquals("validateUserPasswordCaseSensitiveTest: original uppercase password should still work", uppercaseUser, result);
        result = userService.validateUser(mixedCaseUsername, mixedCasePassword);
        assertEquals("validateUserPasswordCaseSensitiveTest: original mixed-case password should still work", mixedCaseUser, result);
    }

    /**
     * Verifies that a nonexistent username fails with an otherwise valid
     * password when the database has zero, one, or two users.
     */
    @Test
    public void validateUserInvalidUserTest() {
        final String username = "testUser";
        final String password = "testPassword";
        final String username2 = "testUser2";
        final String password2 = "testPassword2";
        final String invalidUsername = "missingUser";
        final String invalidPassword = "missingPassword";

        // No users in the database
        //username, password.substring(0, password.length() - 1)
        User result = userService.validateUser(invalidUsername, password);
        assertNull("validateUserInvalidUserValidPasswordTest: should fail with no users", result);
        result = userService.validateUser(invalidUsername, invalidPassword);
        assertNull("validateUserInvalidUserValidPasswordTest: should fail with invalid password and user with no user in the database", result);
        result = userService.validateUser(username.substring(1), password);
        assertNull("validateUserInvalidUserValidPasswordTest: should fail using a valid username with the first letter missing and valid password and no user in the database", result);
        result = userService.validateUser(username.substring(0, username.length() - 1), password);
        assertNull("validateUserInvalidUserValidPasswordTest: should fail using a valid username with the last letter missing and valid password and no user in the database", result);
        result = userService.validateUser(username.substring(1, username.length() - 1), password);
        assertNull("validateUserInvalidUserValidPasswordTest: should fail using a valid username with the first and last letter missing and valid password and no user in the database", result);
        result = userService.validateUser("test User", password);
        assertNull("validateUserInvalidUserValidPasswordTest: should reject a username with a space in the middle with no users", result);
        result = userService.validateUser("", password);
        assertNull("validateUserInvalidUserValidPasswordTest: should reject an empty username with no users", result);
        result = userService.validateUser("    ", password);
        assertNull("validateUserInvalidUserValidPasswordTest: should reject an all-spaces username with no users", result);
        result = userService.validateUser(null, password);
        assertNull("validateUserInvalidUserValidPasswordTest: should reject a null username with no users", result);

        // One user in the database
        User checkUser1 = insertTestingUser(username, password);
        result = userService.validateUser(invalidUsername, password);
        assertNull("validateUserInvalidUserValidPasswordTest: should fail with one user", result);
        result = userService.validateUser(invalidUsername, invalidPassword);
        assertNull("validateUserInvalidUserValidPasswordTest: should fail with invalid password and user with one user in the database", result);
        result = userService.validateUser(username.substring(1), password);
        assertNull("validateUserInvalidUserValidPasswordTest: should fail using a valid username with the first letter missing and valid password and one user in the database", result);
        result = userService.validateUser(username.substring(0, username.length() - 1), password);
        assertNull("validateUserInvalidUserValidPasswordTest: should fail using a valid username with the last letter missing and valid password and one user in the database", result);
        result = userService.validateUser(username.substring(1, username.length() - 1), password);
        assertNull("validateUserInvalidUserValidPasswordTest: should fail using a valid username with the first and last letter missing and valid password and one user in the database", result);
        result = userService.validateUser(username.substring(1), invalidPassword);
        assertNull("validateUserInvalidUserValidPasswordTest: should fail using a valid username with the first letter missing and invalid password and one user in the database", result);
        result = userService.validateUser(username.substring(0, username.length() - 1), invalidPassword);
        assertNull("validateUserInvalidUserValidPasswordTest: should fail using a valid username with the last letter missing and invalid password and one user in the database", result);
        result = userService.validateUser(username.substring(1, username.length() - 1), invalidPassword);
        assertNull("validateUserInvalidUserValidPasswordTest: should fail using a valid username with the first and last letter missing and invalid password and one user in the database", result);
        result = userService.validateUser("test User", password);
        assertNull("validateUserInvalidUserValidPasswordTest: should reject a username with a space in the middle with one user", result);
        result = userService.validateUser("", password);
        assertNull("validateUserInvalidUserValidPasswordTest: should reject an empty username with one user", result);
        result = userService.validateUser("    ", password);
        assertNull("validateUserInvalidUserValidPasswordTest: should reject an all-spaces username with one user", result);
        result = userService.validateUser(null, password);
        assertNull("validateUserInvalidUserValidPasswordTest: should reject a null username with one user", result);

        // Two users in the database
        User checkUser2 = insertTestingUser(username2, password2);
        result = userService.validateUser(invalidUsername, password);
        assertNull("validateUserInvalidUserValidPasswordTest: should fail with two users", result);
        result = userService.validateUser(username.substring(1), password);
        assertNull("validateUserInvalidUserValidPasswordTest: should fail using a valid username with the first letter missing and valid password and two users in the database", result);
        result = userService.validateUser(username.substring(0, username.length() - 1), password);
        assertNull("validateUserInvalidUserValidPasswordTest: should fail using a valid username with the last letter missing and valid password and two users in the database", result);
        result = userService.validateUser(username.substring(1, username.length() - 1), password);
        assertNull("validateUserInvalidUserValidPasswordTest: should fail using a valid username with the first and last letter missing and valid password and two users in the database", result);
        result = userService.validateUser(username2.substring(1), password2);
        assertNull("validateUserInvalidUserValidPasswordTest: should fail using a valid username with the first letter missing and valid password and two users in the database", result);
        result = userService.validateUser(username2.substring(0, username2.length() - 1), password2);
        assertNull("validateUserInvalidUserValidPasswordTest: should fail using a valid username with the last letter missing and valid password and two users in the database", result);
        result = userService.validateUser(username2.substring(1, username2.length() - 1), password2);
        assertNull("validateUserInvalidUserValidPasswordTest: should fail using a valid username with the first and last letter missing and valid password and two users in the database", result);
        result = userService.validateUser(invalidUsername, password2);
        assertNull("validateUserInvalidUserValidPasswordTest: should reject the second user's password too", result);
        result = userService.validateUser(invalidUsername, invalidPassword);
        assertNull("validateUserInvalidUserValidPasswordTest: should fail with invalid password and user when two users in database", result);
        result = userService.validateUser("test User", password);
        assertNull("validateUserInvalidUserValidPasswordTest: should reject a space in the first username", result);
        result = userService.validateUser("", password);
        assertNull("validateUserInvalidUserValidPasswordTest: should reject an empty username with the first user's password", result);
        result = userService.validateUser("    ", password);
        assertNull("validateUserInvalidUserValidPasswordTest: should reject an all-spaces username with the first user's password", result);
        result = userService.validateUser(null, password);
        assertNull("validateUserInvalidUserValidPasswordTest: should reject a null username with the first user's password", result);
        result = userService.validateUser("test User2", password2);
        assertNull("validateUserInvalidUserValidPasswordTest: should reject a space in the second username", result);
        result = userService.validateUser("", password2);
        assertNull("validateUserInvalidUserValidPasswordTest: should reject an empty username with the second user's password", result);
        result = userService.validateUser("    ", password2);
        assertNull("validateUserInvalidUserValidPasswordTest: should reject an all-spaces username with the second user's password", result);
        result = userService.validateUser(null, password2);
        assertNull("validateUserInvalidUserValidPasswordTest: should reject a null username with the second user's password", result);
    }

    /**
     * Verifies that a null username fails with an otherwise valid password
     * when the database has zero, one, or two users.
     */
    @Test
    public void validateUserNullUser() {
        final String username = "testUser";
        final String password = "testPassword";
        final String username2 = "testUser2";
        final String password2 = "testPassword2";
        final String test = "validateUserNullUserValidPasswordTest";

        // No users in the database
        User result = userService.validateUser(null, password);
        assertNull(test + ": should fail with no users", result);

        // One user in the database
        User checkUser = insertTestingUser(username, password);
        result = userService.validateUser(null, password);
        assertNull(test + ": should fail with one user", result);
        assertLogin(test, checkUser, username, password);

        // Two users in the database
        User checkUser2 = insertTestingUser(username2, password2);
        result = userService.validateUser(null, password);
        assertNull(test + ": should reject the first user's password with a null username", result);
        result = userService.validateUser(null, password2);
        assertNull(test + ": should reject the second user's password with a null username", result);
        assertLogin(test, checkUser, username, password);
        assertLogin(test, checkUser2, username2, password2);
    }

    /**
     * Verifies that combinations of invalid usernames and invalid passwords
     * fail when the database has zero, one, or two users.
     */
    @Test
    public void validateUserInvalidUserInvalidPasswordTest() {
        final String[] usernames = {"testUser", "otherUser"};
        final String[] passwords = {"testPassword", "otherPassword"};
        User[] storedUsers = new User[usernames.length];

        for (int userCount = 0; userCount <= usernames.length; userCount++) {
            if (userCount > 0) {
                int newUserIndex = userCount - 1;
                storedUsers[newUserIndex] = insertTestingUser(
                        usernames[newUserIndex], passwords[newUserIndex]);
            }

            for (int account = 0; account < usernames.length; account++) {
                String username = usernames[account];
                String password = passwords[account];

                String[] invalidUsernames = {
                        "missingUser",
                        username.substring(1),
                        username.substring(0, username.length() - 1),
                        username.substring(1, username.length() - 1),
                        "x" + username,
                        username + "x",
                        "x" + username + "x",
                        username.substring(0, 4) + " " + username.substring(4),
                        "",
                        "    ",
                        null
                };

                String[] invalidPasswords = {
                        "incorrectPassword",
                        password.substring(1),
                        password.substring(0, password.length() - 1),
                        password.substring(1, password.length() - 1),
                        "x" + password,
                        password + "x",
                        "x" + password + "x",
                        password + " ",
                        " " + password,
                        " " + password + " ",
                        "        ",
                        "",
                        null,
                        password.toLowerCase(java.util.Locale.ROOT),
                        password.toUpperCase(java.util.Locale.ROOT),
                        "T" + password.substring(1).toLowerCase(java.util.Locale.ROOT)
                };

                for (String invalidUsername : invalidUsernames) {
                    for (String invalidPassword : invalidPasswords) {
                        User result = userService.validateUser(invalidUsername, invalidPassword);
                        assertNull(
                                "validateUserInvalidUserInvalidPasswordTest: should reject username ["
                                        + invalidUsername + "] and password [" + invalidPassword
                                        + "] with " + userCount + " stored users",
                                result);
                    }
                }
            }

            for (int account = 0; account < userCount; account++) {
                User result = userService.validateUser(usernames[account], passwords[account]);
                assertEquals(
                        "validateUserInvalidUserInvalidPasswordTest: original credentials should still work for "
                                + usernames[account] + " with " + userCount + " stored users",
                        storedUsers[account], result);
            }
        }
    }

    /*
     * = = = = = = = = = = =
     * New User Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that mixed-case credentials work when usernames are registered
     * with a leading space, a trailing space, or both. Checks that adding
     * another account preserves the existing accounts.
     */
    @Test
    public void newUserSuccessTest() {
        final String username = "TestUser";
        final String password = "TestPassword";
        final String username2 = "TestUserTwo";
        final String password2 = "TestPasswordTwo";
        final String username3 = "TestUserThree";
        final String password3 = "TestPasswordThree";

        // No users in the database. Register with a leading space.
        User result = userService.newUser(" " + username, password);
        assertNotNull("newUserSuccessTest: should create the first user with a leading space", result);
        String savedUsername = result.getUsername();
        assertEquals("newUserSuccessTest: first username should keep its case and omit the leading space",
                username, savedUsername);
        User loginResult = userService.validateUser(username, password);
        assertEquals("newUserSuccessTest: first user should be able to log in without the leading space",
                result, loginResult);

        // One user in the database. Register with a trailing space.
        User result2 = userService.newUser(username2 + " ", password2);
        assertNotNull("newUserSuccessTest: should create the second user with a trailing space", result2);
        String savedUsername2 = result2.getUsername();
        assertEquals("newUserSuccessTest: second username should keep its case and omit the trailing space",
                username2, savedUsername2);
        User loginResult2 = userService.validateUser(username2, password2);
        assertEquals("newUserSuccessTest: second user should be able to log in without the trailing space",
                result2, loginResult2);
        loginResult = userService.validateUser(username, password);
        assertEquals("newUserSuccessTest: first user should still be able to log in",
                result, loginResult);
        assertNotEquals("newUserSuccessTest: first and second users should differ", result, result2);

        // Two users in the database. Register with spaces on both ends.
        User result3 = userService.newUser(" " + username3 + " ", password3);
        assertNotNull("newUserSuccessTest: should create the third user with spaces on both ends", result3);
        String savedUsername3 = result3.getUsername();
        assertEquals("newUserSuccessTest: third username should keep its case and omit both spaces",
                username3, savedUsername3);
        User loginResult3 = userService.validateUser(username3, password3);
        assertEquals("newUserSuccessTest: third user should be able to log in without either space",
                result3, loginResult3);

        loginResult = userService.validateUser(username, password);
        assertEquals("newUserSuccessTest: first user should still be able to log in after adding the third user",
                result, loginResult);
        loginResult2 = userService.validateUser(username2, password2);
        assertEquals("newUserSuccessTest: second user should still be able to log in after adding the third user",
                result2, loginResult2);
        assertNotEquals("newUserSuccessTest: first and third users should differ", result, result3);
        assertNotEquals("newUserSuccessTest: second and third users should differ", result2, result3);
    }

    /**
     * Verifies that an existing username cannot be registered with a
     * different password, regardless of username casing.
     */
    @Test
    public void newUserExistingUserTest() {
        final String username = "testUser";
        final String password = "testPassword";
        final String username2 = "testUser2";
        final String password2 = "testPassword2";
        final String newPassword = "differentPassword";

        // One user in the database
        User checkUser = insertTestingUser(username, password);

        User result = userService.newUser(username, newPassword);
        assertNull("newUserExistingUserTest: should reject the original username with one user", result);
        result = userService.newUser("testuser", newPassword);
        assertNull("newUserExistingUserTest: should reject the lowercase username with one user", result);
        result = userService.newUser("TESTUSER", newPassword);
        assertNull("newUserExistingUserTest: should reject the uppercase username with one user", result);
        result = userService.newUser("TeStUsEr", newPassword);
        assertNull("newUserExistingUserTest: should reject a different mixed-case username with one user", result);

        User originalLogin = userService.validateUser(username, password);
        assertEquals("newUserExistingUserTest: original credentials should still work with one user",
                checkUser, originalLogin);
        User rejectedLogin = userService.validateUser(username, newPassword);
        assertNull("newUserExistingUserTest: attempted password should fail with one user", rejectedLogin);

        // Two users in the database
        User checkUser2 = insertTestingUser(username2, password2);

        // Duplicate registration attempts for the first user
        result = userService.newUser(username, newPassword);
        assertNull("newUserExistingUserTest: should reject the first user's original username", result);
        result = userService.newUser("testuser", newPassword);
        assertNull("newUserExistingUserTest: should reject the first user's lowercase username", result);
        result = userService.newUser("TESTUSER", newPassword);
        assertNull("newUserExistingUserTest: should reject the first user's uppercase username", result);
        result = userService.newUser("TeStUsEr", newPassword);
        assertNull("newUserExistingUserTest: should reject a different mixed-case spelling of the first username", result);

        // Duplicate registration attempts for the second user
        result = userService.newUser(username2, newPassword);
        assertNull("newUserExistingUserTest: should reject the second user's original username", result);
        result = userService.newUser("testuser2", newPassword);
        assertNull("newUserExistingUserTest: should reject the second user's lowercase username", result);
        result = userService.newUser("TESTUSER2", newPassword);
        assertNull("newUserExistingUserTest: should reject the second user's uppercase username", result);
        result = userService.newUser("TeStUsEr2", newPassword);
        assertNull("newUserExistingUserTest: should reject a different mixed-case spelling of the second username", result);

        originalLogin = userService.validateUser(username, password);
        assertEquals("newUserExistingUserTest: first user's original credentials should still work",
                checkUser, originalLogin);
        User originalLogin2 = userService.validateUser(username2, password2);
        assertEquals("newUserExistingUserTest: second user's original credentials should still work",
                checkUser2, originalLogin2);

        rejectedLogin = userService.validateUser(username, newPassword);
        assertNull("newUserExistingUserTest: first user should reject the attempted password", rejectedLogin);
        User rejectedLogin2 = userService.validateUser(username2, newPassword);
        assertNull("newUserExistingUserTest: second user should reject the attempted password", rejectedLogin2);
    }

    /**
     * Verifies that an existing username cannot be registered again with
     * the same password.
     */
    @Test
    public void newUserExistingUserExistingUserPasswordTest() {
        final String username = "testUser";
        final String password = "testPassword";
        final String username2 = "testUser2";
        final String password2 = "testPassword2";

        // One user in the database
        User checkUser = insertTestingUser(username, password);
        User result = userService.newUser(username, password);
        assertNull("newUserExistingUserExistingUserPasswordTest: should reject duplicate credentials", result);

        // Two users in the database
        User checkUser2 = insertTestingUser(username2, password2);
        result = userService.newUser(username, password);
        assertNull("newUserExistingUserExistingUserPasswordTest: should still reject the first user", result);
        result = userService.newUser(username2, password2);
        assertNull("newUserExistingUserExistingUserPasswordTest: should reject the second user", result);
        assertEquals("newUserExistingUserExistingUserPasswordTest: first user should remain unchanged",
                checkUser, userService.validateUser(username, password));
        assertEquals("newUserExistingUserExistingUserPasswordTest: second user should remain unchanged",
                checkUser2, userService.validateUser(username2, password2));
    }

    /**
     * Verifies that a null username cannot be registered when the database
     * has zero, one, or two users.
     */
    @Test
    public void newUserInvalidUserTest() {
        final String username = "testUser";
        final String password = "testPassword";
        final String username2 = "testUser2";
        final String password2 = "testPassword2";

        // No users in the database
        User result = userService.newUser(null, password);
        assertNull("newUserNullUserTest: should fail with no users", result);

        // One user in the database
        insertTestingUser(username, password);
        result = userService.newUser(null, password);
        assertNull("newUserNullUserTest: should fail with one user", result);

        // Two users in the database
        insertTestingUser(username2, password2);
        result = userService.newUser(null, password2);
        assertNull("newUserNullUserTest: should fail with two users", result);
    }

    /**
     * Verifies that a null password cannot be used for an available or
     * existing username.
     */
    @Test
    public void newUserNullPasswordTest() {
        final String username = "testUser";
        final String password = "testPassword";
        final String username2 = "testUser2";
        final String password2 = "testPassword2";
        final String newUsername = "differentTestUser";

        // No users in the database
        User result = userService.newUser(newUsername, null);
        assertNull("newUserNullPasswordTest: should fail with no users", result);

        // One user in the database
        User checkUser = insertTestingUser(username, password);
        result = userService.newUser(newUsername, null);
        assertNull("newUserNullPasswordTest: should reject an available username with a null password", result);
        result = userService.newUser(username, null);
        assertNull("newUserNullPasswordTest: should reject an existing username with a null password", result);

        // Two users in the database
        User checkUser2 = insertTestingUser(username2, password2);
        result = userService.newUser(newUsername, null);
        assertNull("newUserNullPasswordTest: should still reject an available username with a null password", result);
        result = userService.newUser(username, null);
        assertNull("newUserNullPasswordTest: should still reject the first username", result);
        result = userService.newUser(username2, null);
        assertNull("newUserNullPasswordTest: should reject the second username", result);
        assertEquals("newUserNullPasswordTest: first user should remain unchanged",
                checkUser, userService.validateUser(username, password));
        assertEquals("newUserNullPasswordTest: second user should remain unchanged",
                checkUser2, userService.validateUser(username2, password2));
    }

    /**
     * Verifies that null registration credentials fail when the database
     * has zero, one, or two users.
     */
    @Test
    public void newUserNullUserNullPasswordTest() {
        final String username = "testUser";
        final String password = "testPassword";
        final String username2 = "testUser2";
        final String password2 = "testPassword2";

        // No users in the database
        User result = userService.newUser(null, null);
        assertNull("newUserNullUserNullPasswordTest: should fail with no users", result);

        // One user in the database
        insertTestingUser(username, password);
        result = userService.newUser(null, null);
        assertNull("newUserNullUserNullPasswordTest: should fail with one user", result);

        // Two users in the database
        insertTestingUser(username2, password2);
        result = userService.newUser(null, null);
        assertNull("newUserNullUserNullPasswordTest: should fail with two users", result);
    }

    /*
     * = = = = = = = = = = =
     * Rename User Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that renaming a user updates only the selected user's name.
     */
    @Test
    public void renameUserSuccessTest() {
        final String username = "testUser";
        final String password = "testPassword";
        final String username2 = "testUser2";
        final String password2 = "testPassword2";
        final String newName = "NewTestName";
        final String newName2 = "OtherTestName";

        // One user in the database
        User checkUser = insertTestingUser(username, password);
        boolean result = userService.renameUser(checkUser, newName);
        assertTrue("renameUserSuccessTest: should rename the first user", result);
        assertEquals("renameUserSuccessTest: first user's name should be updated",
                newName, checkUser.getName());

        // Two users in the database
        User checkUser2 = insertTestingUser(username2, password2);
        String originalName2 = checkUser2.getName();

        result = userService.renameUser(checkUser, newName2);
        assertTrue("renameUserSuccessTest: should rename the first user again", result);
        assertEquals("renameUserSuccessTest: first user's name should change again",
                newName2, checkUser.getName());
        assertEquals("renameUserSuccessTest: second user's name should remain unchanged",
                originalName2, checkUser2.getName());

        result = userService.renameUser(checkUser2, newName);
        assertTrue("renameUserSuccessTest: should rename the second user", result);
        assertEquals("renameUserSuccessTest: second user's name should be updated",
                newName, checkUser2.getName());
        assertEquals("renameUserSuccessTest: first user's name should remain unchanged",
                newName2, checkUser.getName());

        assertEquals("renameUserSuccessTest: first user's login should still work",
                checkUser, userService.validateUser(username, password));
        assertEquals("renameUserSuccessTest: second user's login should still work",
                checkUser2, userService.validateUser(username2, password2));
    }

    /**
     * Verifies that renaming a null user fails with zero, one, or two users.
     */
    @Test
    public void renameUserNullUserTest() {
        final String username = "testUser";
        final String password = "testPassword";
        final String username2 = "testUser2";
        final String password2 = "testPassword2";
        final String newName = "NewTestName";

        // No users in the database
        boolean result = userService.renameUser(null, newName);
        assertFalse("renameUserNullUserTest: should fail with no users", result);

        // One user in the database
        User checkUser = insertTestingUser(username, password);
        String originalName = checkUser.getName();
        result = userService.renameUser(null, newName);
        assertFalse("renameUserNullUserTest: should fail with one user", result);
        assertEquals("renameUserNullUserTest: first user's name should remain unchanged",
                originalName, checkUser.getName());

        // Two users in the database
        User checkUser2 = insertTestingUser(username2, password2);
        String originalName2 = checkUser2.getName();
        result = userService.renameUser(null, newName);
        assertFalse("renameUserNullUserTest: should fail with two users", result);
        assertEquals("renameUserNullUserTest: first user's name should still be unchanged",
                originalName, checkUser.getName());
        assertEquals("renameUserNullUserTest: second user's name should remain unchanged",
                originalName2, checkUser2.getName());
    }

    /**
     * Verifies that a null name is rejected without changing either user.
     */
    @Test
    public void renameUserNullNameTest() {
        final String username = "testUser";
        final String password = "testPassword";
        final String username2 = "testUser2";
        final String password2 = "testPassword2";

        // One user in the database
        User checkUser = insertTestingUser(username, password);
        String originalName = checkUser.getName();
        boolean result = userService.renameUser(checkUser, null);
        assertFalse("renameUserNullNameTest: should reject a null name", result);
        assertEquals("renameUserNullNameTest: first user's name should remain unchanged",
                originalName, checkUser.getName());

        // Two users in the database
        User checkUser2 = insertTestingUser(username2, password2);
        String originalName2 = checkUser2.getName();
        result = userService.renameUser(checkUser, null);
        assertFalse("renameUserNullNameTest: should still reject a null name for the first user", result);
        result = userService.renameUser(checkUser2, null);
        assertFalse("renameUserNullNameTest: should reject a null name for the second user", result);
        assertEquals("renameUserNullNameTest: first user's name should still be unchanged",
                originalName, checkUser.getName());
        assertEquals("renameUserNullNameTest: second user's name should remain unchanged",
                originalName2, checkUser2.getName());
    }

    /**
     * Verifies that a name shorter than two characters is rejected.
     */
    @Test
    public void renameUserShortNameTest() {
        final String username = "testUser";
        final String password = "testPassword";
        final String username2 = "testUser2";
        final String password2 = "testPassword2";
        final String newName = "A";

        // One user in the database
        User checkUser = insertTestingUser(username, password);
        String originalName = checkUser.getName();
        boolean result = userService.renameUser(checkUser, newName);
        assertFalse("renameUserShortNameTest: should reject a short name", result);
        assertEquals("renameUserShortNameTest: first user's name should remain unchanged",
                originalName, checkUser.getName());

        // Two users in the database
        User checkUser2 = insertTestingUser(username2, password2);
        String originalName2 = checkUser2.getName();
        result = userService.renameUser(checkUser, newName);
        assertFalse("renameUserShortNameTest: should still reject a short name for the first user", result);
        result = userService.renameUser(checkUser2, newName);
        assertFalse("renameUserShortNameTest: should reject a short name for the second user", result);
        assertEquals("renameUserShortNameTest: first user's name should still be unchanged",
                originalName, checkUser.getName());
        assertEquals("renameUserShortNameTest: second user's name should remain unchanged",
                originalName2, checkUser2.getName());
    }

    /**
     * Verifies that a name longer than forty characters is rejected.
     */
    @Test
    public void renameUserLongNameTest() {
        final String username = "testUser";
        final String password = "testPassword";
        final String username2 = "testUser2";
        final String password2 = "testPassword2";
        final String newName = "ThisNameIsMuchLongerThanTheAllowedFortyCharacters";

        // One user in the database
        User checkUser = insertTestingUser(username, password);
        String originalName = checkUser.getName();
        boolean result = userService.renameUser(checkUser, newName);
        assertFalse("renameUserLongNameTest: should reject a long name", result);
        assertEquals("renameUserLongNameTest: first user's name should remain unchanged",
                originalName, checkUser.getName());

        // Two users in the database
        User checkUser2 = insertTestingUser(username2, password2);
        String originalName2 = checkUser2.getName();
        result = userService.renameUser(checkUser, newName);
        assertFalse("renameUserLongNameTest: should still reject a long name for the first user", result);
        result = userService.renameUser(checkUser2, newName);
        assertFalse("renameUserLongNameTest: should reject a long name for the second user", result);
        assertEquals("renameUserLongNameTest: first user's name should still be unchanged",
                originalName, checkUser.getName());
        assertEquals("renameUserLongNameTest: second user's name should remain unchanged",
                originalName2, checkUser2.getName());
    }

    /*
     * = = = = = = = = = = =
     * Change Password Tests
     * = = = = = = = = = = =
     */

    /**
     * Verifies that changing a password updates only the selected user.
     */
    @Test
    public void changeUsersPasswordSuccessTest() {
        final String username = "testUser";
        final String password = "testPassword";
        final String username2 = "testUser2";
        final String password2 = "testPassword2";
        final String newPassword = "NewTestPassword";
        final String newPassword2 = "OtherTestPassword";

        // One user in the database
        User checkUser = insertTestingUser(username, password);
        boolean result = userService.changeUsersPassword(checkUser, newPassword);
        assertTrue("changeUsersPasswordSuccessTest: should change the first user's password", result);
        assertEquals("changeUsersPasswordSuccessTest: new password should log in the first user",
                checkUser, userService.validateUser(username, newPassword));
        assertNull("changeUsersPasswordSuccessTest: original password should no longer work",
                userService.validateUser(username, password));

        // Two users in the database
        User checkUser2 = insertTestingUser(username2, password2);
        result = userService.changeUsersPassword(checkUser, newPassword2);
        assertTrue("changeUsersPasswordSuccessTest: should change the first user's password again", result);
        assertEquals("changeUsersPasswordSuccessTest: newest password should work",
                checkUser, userService.validateUser(username, newPassword2));
        assertNull("changeUsersPasswordSuccessTest: previous password should no longer work",
                userService.validateUser(username, newPassword));
        assertEquals("changeUsersPasswordSuccessTest: second user's password should remain unchanged",
                checkUser2, userService.validateUser(username2, password2));

        result = userService.changeUsersPassword(checkUser2, newPassword);
        assertTrue("changeUsersPasswordSuccessTest: should change the second user's password", result);
        assertEquals("changeUsersPasswordSuccessTest: second user's new password should work",
                checkUser2, userService.validateUser(username2, newPassword));
        assertNull("changeUsersPasswordSuccessTest: second user's original password should no longer work",
                userService.validateUser(username2, password2));
        assertEquals("changeUsersPasswordSuccessTest: first user's password should remain unchanged",
                checkUser, userService.validateUser(username, newPassword2));
    }

    /**
     * Verifies that changing a null user's password fails with zero, one,
     * or two users.
     */
    @Test
    public void changeUsersPasswordNullUserTest() {
        final String username = "testUser";
        final String password = "testPassword";
        final String username2 = "testUser2";
        final String password2 = "testPassword2";
        final String newPassword = "NewTestPassword";

        // No users in the database
        boolean result = userService.changeUsersPassword(null, newPassword);
        assertFalse("changeUsersPasswordNullUserTest: should fail with no users", result);

        // One user in the database
        User checkUser = insertTestingUser(username, password);
        result = userService.changeUsersPassword(null, newPassword);
        assertFalse("changeUsersPasswordNullUserTest: should fail with one user", result);
        assertEquals("changeUsersPasswordNullUserTest: first user's password should remain unchanged",
                checkUser, userService.validateUser(username, password));

        // Two users in the database
        User checkUser2 = insertTestingUser(username2, password2);
        result = userService.changeUsersPassword(null, newPassword);
        assertFalse("changeUsersPasswordNullUserTest: should fail with two users", result);
        assertEquals("changeUsersPasswordNullUserTest: first user's password should still be unchanged",
                checkUser, userService.validateUser(username, password));
        assertEquals("changeUsersPasswordNullUserTest: second user's password should remain unchanged",
                checkUser2, userService.validateUser(username2, password2));
    }

    /**
     * Verifies that a null password is rejected without changing either user.
     */
    @Test
    public void changeUsersPasswordNullPasswordTest() {
        final String username = "testUser";
        final String password = "testPassword";
        final String username2 = "testUser2";
        final String password2 = "testPassword2";

        // One user in the database
        User checkUser = insertTestingUser(username, password);
        boolean result = userService.changeUsersPassword(checkUser, null);
        assertFalse("changeUsersPasswordNullPasswordTest: should reject a null password", result);
        assertEquals("changeUsersPasswordNullPasswordTest: original password should still work",
                checkUser, userService.validateUser(username, password));

        // Two users in the database
        User checkUser2 = insertTestingUser(username2, password2);
        result = userService.changeUsersPassword(checkUser, null);
        assertFalse("changeUsersPasswordNullPasswordTest: should still reject a null password for the first user", result);
        result = userService.changeUsersPassword(checkUser2, null);
        assertFalse("changeUsersPasswordNullPasswordTest: should reject a null password for the second user", result);
        assertEquals("changeUsersPasswordNullPasswordTest: first user's original password should still work",
                checkUser, userService.validateUser(username, password));
        assertEquals("changeUsersPasswordNullPasswordTest: second user's original password should still work",
                checkUser2, userService.validateUser(username2, password2));
    }

    /**
     * Verifies that a password shorter than eight characters is rejected.
     */
    @Test
    public void changeUsersPasswordShortPasswordTest() {
        final String username = "testUser";
        final String password = "testPassword";
        final String username2 = "testUser2";
        final String password2 = "testPassword2";
        final String newPassword = "Short";

        // One user in the database
        User checkUser = insertTestingUser(username, password);
        boolean result = userService.changeUsersPassword(checkUser, newPassword);
        assertFalse("changeUsersPasswordShortPasswordTest: should reject a short password", result);
        assertEquals("changeUsersPasswordShortPasswordTest: original password should still work",
                checkUser, userService.validateUser(username, password));
        assertNull("changeUsersPasswordShortPasswordTest: rejected password should not work",
                userService.validateUser(username, newPassword));

        // Two users in the database
        User checkUser2 = insertTestingUser(username2, password2);
        result = userService.changeUsersPassword(checkUser, newPassword);
        assertFalse("changeUsersPasswordShortPasswordTest: should still reject a short password for the first user", result);
        result = userService.changeUsersPassword(checkUser2, newPassword);
        assertFalse("changeUsersPasswordShortPasswordTest: should reject a short password for the second user", result);
        assertEquals("changeUsersPasswordShortPasswordTest: first user's original password should still work",
                checkUser, userService.validateUser(username, password));
        assertEquals("changeUsersPasswordShortPasswordTest: second user's original password should still work",
                checkUser2, userService.validateUser(username2, password2));
        assertNull("changeUsersPasswordShortPasswordTest: first user should reject the short password",
                userService.validateUser(username, newPassword));
        assertNull("changeUsersPasswordShortPasswordTest: second user should reject the short password",
                userService.validateUser(username2, newPassword));
    }

    /**
     * Verifies that a password longer than thirty characters is rejected.
     */
    @Test
    public void changeUsersPasswordLongPasswordTest() {
        final String username = "testUser";
        final String password = "testPassword";
        final String username2 = "testUser2";
        final String password2 = "testPassword2";
        final String newPassword = "ThisPasswordIsMuchLongerThanTheAllowedThirtyCharacters";

        // One user in the database
        User checkUser = insertTestingUser(username, password);
        boolean result = userService.changeUsersPassword(checkUser, newPassword);
        assertFalse("changeUsersPasswordLongPasswordTest: should reject a long password", result);
        assertEquals("changeUsersPasswordLongPasswordTest: original password should still work",
                checkUser, userService.validateUser(username, password));
        assertNull("changeUsersPasswordLongPasswordTest: rejected password should not work",
                userService.validateUser(username, newPassword));

        // Two users in the database
        User checkUser2 = insertTestingUser(username2, password2);
        result = userService.changeUsersPassword(checkUser, newPassword);
        assertFalse("changeUsersPasswordLongPasswordTest: should still reject a long password for the first user", result);
        result = userService.changeUsersPassword(checkUser2, newPassword);
        assertFalse("changeUsersPasswordLongPasswordTest: should reject a long password for the second user", result);
        assertEquals("changeUsersPasswordLongPasswordTest: first user's original password should still work",
                checkUser, userService.validateUser(username, password));
        assertEquals("changeUsersPasswordLongPasswordTest: second user's original password should still work",
                checkUser2, userService.validateUser(username2, password2));
        assertNull("changeUsersPasswordLongPasswordTest: first user should reject the long password",
                userService.validateUser(username, newPassword));
        assertNull("changeUsersPasswordLongPasswordTest: second user should reject the long password",
                userService.validateUser(username2, newPassword));
    }

    /**
     * Verifies that a password containing whitespace is rejected.
     */
    @Test
    public void changeUsersPasswordWhitespaceTest() {
        final String username = "testUser";
        final String password = "testPassword";
        final String username2 = "testUser2";
        final String password2 = "testPassword2";
        final String newPassword = "New Test Password";

        // One user in the database
        User checkUser = insertTestingUser(username, password);
        boolean result = userService.changeUsersPassword(checkUser, newPassword);
        assertFalse("changeUsersPasswordWhitespaceTest: should reject whitespace in a password", result);
        assertEquals("changeUsersPasswordWhitespaceTest: original password should still work",
                checkUser, userService.validateUser(username, password));
        assertNull("changeUsersPasswordWhitespaceTest: rejected password should not work",
                userService.validateUser(username, newPassword));

        // Two users in the database
        User checkUser2 = insertTestingUser(username2, password2);
        result = userService.changeUsersPassword(checkUser, newPassword);
        assertFalse("changeUsersPasswordWhitespaceTest: should still reject whitespace for the first user", result);
        result = userService.changeUsersPassword(checkUser2, newPassword);
        assertFalse("changeUsersPasswordWhitespaceTest: should reject whitespace for the second user", result);
        assertEquals("changeUsersPasswordWhitespaceTest: first user's original password should still work",
                checkUser, userService.validateUser(username, password));
        assertEquals("changeUsersPasswordWhitespaceTest: second user's original password should still work",
                checkUser2, userService.validateUser(username2, password2));
        assertNull("changeUsersPasswordWhitespaceTest: first user should reject the password containing whitespace",
                userService.validateUser(username, newPassword));
        assertNull("changeUsersPasswordWhitespaceTest: second user should reject the password containing whitespace",
                userService.validateUser(username2, newPassword));
    }

    /**
     * Creates a test user and verifies that the saved credentials work.
     */
    private User insertTestingUser(String username, String password) {
        User user = userService.validateUser(username, password);
        assertNull("insertTestingUser: user should not exist yet", user);

        user = userService.newUser(username, password);
        assertNotNull("insertTestingUser: user should exist after creation", user);
        assertEquals("insertTestingUser: user should have the correct username", username, user.getUsername());

        User userCheck = userService.validateUser(username, password);
        assertNotNull("insertTestingUser: user should exist after validation", userCheck);
        assertEquals("insertTestingUser: validated user should match the created user", user, userCheck);
        assertEquals("inserTestingUser: validate user should have the same username still", userCheck.getUsername(), username);
        return user;
    }

    private User assertLogin(String test, User expectedUser, String username, String password) {
        User result = userService.validateUser(username, password);
        assertNotNull(test + ": " + username + " should be able to log in", result);
        assertEquals(test + ": login should return the expected user for " + username, expectedUser, result);
        assertEquals(test + ": login should return the expected username", username, result.getUsername());
        return result;
    }
}