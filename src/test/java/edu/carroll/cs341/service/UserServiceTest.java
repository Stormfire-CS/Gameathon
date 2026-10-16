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
        if (users.isEmpty())
            userRepo.save(fakeUser);
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
}