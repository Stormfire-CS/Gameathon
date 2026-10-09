package edu.carroll.cs341.service;

import edu.carroll.cs341.jpa.model.Login;
import edu.carroll.cs341.jpa.repo.LoginRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService{
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final LoginRepository loginRepo;

    public UserServiceImpl(LoginRepository loginRepo) {
        this.loginRepo = loginRepo;
    }

    /**
     * Determine if the information provided is valid, and the user exists in the system.
     * @param username - The username of the person trying to log in.
     * @param password - The password entered by the person trying to log in.
     */
    @Override
    public boolean validateUser(String username, String password) {
        log.debug("validateUser: user '{}' attempted login", username);

        // Always do the lookup in a case-insensitive manner (lower-casing the data).
        List<Login> users = loginRepo.findByUsernameIgnoreCase(username);

        // We expect 0 or 1, so if we get more than 1, bail out as this is an error we don't deal with properly.
        if (users.size() != 1) {
            log.debug("validateUser: found {} users", users.size());
            return false;
        }
        Login u = users.getFirst();
        // XXX - Using Java's hashCode is wrong on SO many levels, but is good enough for demonstration purposes.
        // NEVER EVER do this in production code!
        final String userProvidedHash = Integer.toString(password.hashCode());
        if (!u.getHashedPassword().equals(userProvidedHash)) {
            log.debug("validateUser: password !match");
            return false;
        }

        // User exists, and the provided password matches the hashed password in the database.
        log.info("validateUser: successful login for {}", username);
        return true;
    }

    @Override
    public boolean validateNewUser(String username, String password1, String password2) {
        log.debug("validateNewUser: attempting to register username '{}'", username);

        List<Login> users =  loginRepo.findByUsernameIgnoreCase(username);
        if (!users.isEmpty()) {
            log.debug("validateNewUser: username '{}' already exists", username);
            return false;
        }
        if (!password1.equals(password2)) {
            log.debug("validateNewUser: passwords do not match for '{}'", username);
            return false;
        }

        Login user = new Login(username, password1);
        loginRepo.save(user);

        log.info("validateNewUser: successfully registered user '{}'", username);
        return true;
    }

    @Override
    public Long getUserID(String username) {
        log.debug("getUserID: looking up ID for '{}'", username);
        List<Login> users = loginRepo.findByUsernameIgnoreCase(username);

        if (users.size() != 1) {
            log.debug("getUserID: found {} users for '{}'", users.size(), username);
            return null;
        }

        Long userID = users.getFirst().getId();

        log.debug("getUserID: found ID {} for '{}'", userID, username);

        return userID;
    }
}
