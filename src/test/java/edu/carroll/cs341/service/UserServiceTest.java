package edu.carroll.cs341.service;

import java.util.List;

import edu.carroll.cs341.jpa.model.User;
import edu.carroll.cs341.jpa.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.springframework.test.util.AssertionErrors.*;

@SpringBootTest
public class UserServiceTest {
    private static final String username = "testuser";
    private static final String password = "testpass";
    private static final String newUser = "testingNewUser";
    private static final String newPass = "passwordcheck";
    private final String validNewUser = newUser + System.nanoTime();

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepo;

    private User fakeUser = new User(username, password);

    @BeforeEach
    public void beforeTest() {
        assertNotNull("loginRepository must be injected", userRepo);
        assertNotNull("loginService must be injected", userService);

        // Ensure dummy record is in the DB
        final List<User> users = userRepo.findByUsernameIgnoreCase(username);
        if (users.isEmpty()) {
            userRepo.save(fakeUser);
        }
        final List<User> newUsers = userRepo.findByUsernameIgnoreCase(newUser);
        assertTrue("newUser should not already exist in the database", newUsers.isEmpty());
    }

    @Test
    public void validateUserSuccessTest() {
        assertNotNull("validateUserSuccessTest: should succeed using the same user/pass info", userService.validateUser(username, password));
    }

    @Test
    public void validateUserExistingUserInvalidPasswordTest() {
        assertNull("validateUserExistingUserInvalidPasswordTest: should fail using a valid user, invalid pass", userService.validateUser(username, password + "extra"));
    }

    @Test
    public void validateUserInvalidUserValidPasswordTest() {
        assertNull("validateUserInvalidUserValidPasswordTest: should fail using an invalid user, valid pass", userService.validateUser(username + "not", password));
    }

    @Test
    public void validateUserInvalidUserInvalidPasswordTest() {
        assertNull("validateUserInvalidUserInvalidPasswordTest: should fail using an invalid user, valid pass", userService.validateUser(username + "not", password + "extra"));
    }

    @Test
    public void validateUserNullUserValidPasswordTest(){
        assertNull("validateUserNullUserInvalidPasswordTest: should fail using a null user, valid pass", userService.validateUser(null, password));
    }

    @Test
    public void validateUserValidUserNullPasswordTest(){
        assertNull("validateUserValidUserNullPasswordTest: should fail using a valid user, null pass", userService.validateUser(username, null));
    }

    @Test
    public void validateUserNullUserInvalidPasswordTest(){
        assertNull("validateUserNullUserInvalidPasswordTest: should fail using a null user, invalid pass", userService.validateUser(null, password + "extra"));
    }

    @Test
    public void validateUserInvalidUserNullPasswordTest(){
        assertNull("validateUserInvalidUserNullPasswordTest: should fail using a invalid user, null pass", userService.validateUser(username + "not", null));
    }

    @Test
    public void validateUserNullUserNullPasswordTest(){
        assertNull("validateUserNullUserNullPasswordTest: should fail using a null user, null pass", userService.validateUser(null, null));
    }

    @Test
    public void validateNewUserExistingUserPasswordsMatchTest () {
        assertNull("validateNewUserExistingUserPasswordsMatchTest: should fail using an existing user, matching passwords", userService.validateNewUser(username, newPass, newPass));
    }

    @Test
    public void validateNewUserNullUserPasswordsMatchTest () {
        assertNull("validateNewUserNullUserPasswordsMatchTest: should fail using an null user, matching passwords", userService.validateNewUser(null, newPass, newPass));
    }

    @Test
    public void validateNewUserExistingUserExistingUserPasswordTest () {
        assertNull("validateNewUserExistingUserExistingUserPasswordTest: should fail using an existing user, existing users matching passwords", userService.validateNewUser(username, password, password));

    }

    @Test
    public void validateNewUserNullUserExistingUserPasswordsTest () {
        assertNull("validateNewUserNullUserExistingUserPasswordsTest: should fail using a null user, existing users matching passwords", userService.validateNewUser(null, password, password));
    }

    @Test
    public void validateNewUserValidUserNullPasswordValidPasswordTest () {
        assertNull("validateNewUserValidUserNullPasswordValidPasswordTest: should fail using a valid user, null password, valid password", userService.validateNewUser(newUser, null, newPass));

    }

    @Test
    public void validateNewUserExistingUserNullPasswordValidPasswordTest () {
        assertNull("validateNewUserExistingUserNullPasswordValidPasswordTest: should fail using an existing user, null password, valid password", userService.validateNewUser(username, null, newPass));
    }

    @Test
    public void validateNewUserNullUserNullPasswordValidPasswordTest () {
        assertNull("validateNewUserNullUserNullPasswordValidPasswordTest: should fail using a null user, null password, valid password", userService.validateNewUser(null, null, newPass));
    }

    @Test
    public void validateNewUserValidUserValidPasswordNullPasswordTest () {
        assertNull("validateNewUserValidUserValidPasswordNullPasswordTest: should fail using a valid user, valid password, null password", userService.validateNewUser(newUser, newPass,null));

    }

    @Test
    public void validateNewUserExistingUserValidPasswordNullPasswordTest () {
        assertNull("validateNewUserExistingUserValidPasswordNullPasswordTest: should fail using an existing user, valid password, null password", userService.validateNewUser(username, newPass,null));
    }

    @Test
    public void validateNewUserNullUserValidPasswordNullPasswordTest () {
        assertNull("validateNewUserNullUserValidPasswordNullPasswordTest: should fail using a null user, valid password, null password", userService.validateNewUser(null, newPass,null));
    }

    @Test
    public void validateNewUserValidUserNullPasswordsTest () {
        assertNull("validateNewUserValidUserNullPasswordsTest: should fail using a valid user, null passwords", userService.validateNewUser(newUser, null,null));

    }

    @Test
    public void validateNewUserExistingUserNullPasswordsTest () {
        assertNull("validateNewUserExistingUserNullPasswordsTest: should fail using an existing user, null passwords", userService.validateNewUser(username, null,null));
    }

    @Test
    public void validateNewUserNullUserNullPasswordsTest () {
        assertNull("validateNewUserNullUserNullPasswordsTest: should fail using a null user, null passwords", userService.validateNewUser(null, null, null));
    }

    @Test
    public void validateNewUserValidUserNonMatchingPasswordsTest () {
        assertNull("validateNewUserValidUserNonMatchingPasswordsTest: should fail using a valid user, non matching passwords", userService.validateNewUser(newUser, password,newPass));

    }

    @Test
    public void validateNewUserExistingUserNonMatchingPasswordsTest () {
        assertNull("validateNewUserExistingUserNonMatchingPasswordsTest: should fail using an existing user, non matching passwords", userService.validateNewUser(username, password,newPass));
    }

    @Test
    public void validateNewUserNullUserNonMatchingPasswordsTest () {
        assertNull("validateNewUserNullUserNonMatchingPasswordsTest: should fail using a null user, non matching passwords", userService.validateNewUser(null, password,newPass));
    }

    @Test
    public void validateNewUserValidUserMatchingPasswordsTest () {
        assertNotNull("validateNewUserValidUserMatchingPasswordsTest: should work using valid user, matching passwords", userService.validateNewUser(validNewUser, password, password));
    }


}