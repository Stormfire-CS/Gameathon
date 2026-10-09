package edu.carroll.cs341.service;

import edu.carroll.cs341.jpa.model.Login;
import edu.carroll.cs341.jpa.repo.LoginRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NewUserServiceImpl implements NewUserService{
    private final LoginRepository loginRepo;

    public NewUserServiceImpl(LoginRepository loginRepo) {
        this.loginRepo = loginRepo;
    }

    @Override
    public boolean validateNewUser(String username, String password1, String password2) {
        List<Login> users =  loginRepo.findByUsernameIgnoreCase(username);
        if (!users.isEmpty()) {
            return false;
        }
        if (!password1.equals(password2)) {
            return false;
        }

        Login user = new Login(username, password1);
        loginRepo.save(user);

        return true;
    }

    @Override
    public Long getUserID(String username) {
        List<Login> users = loginRepo.findByUsernameIgnoreCase(username);

        if (users.size() != 1) {
            return null;
        }

        return users.getFirst().getId();
    }
}
