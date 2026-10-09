package edu.carroll.cs341.service;

public interface NewUserService {
    boolean validateNewUser(String username, String password1, String password2);
    Long getUserID(String username);
}
