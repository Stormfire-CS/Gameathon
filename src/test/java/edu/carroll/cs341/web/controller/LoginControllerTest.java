package edu.carroll.cs341.web.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.carroll.cs341.jpa.model.User;
import edu.carroll.cs341.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(LoginController.class)
@AutoConfigureRestTestClient
public class LoginControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    public void loginPageTest() throws Exception {
        mockMvc.perform(get("/login")).andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<h1>Login</h1>")))
                .andExpect(content().string(containsString("action=\"/login\"")));
    }

    @Test
    public void loginSuccessTest() throws Exception {
        User user = new User("testuser", "testpassword");
        when(userService.validateUser("testuser", "testpassword")).thenReturn(user);

        mockMvc.perform(post("/login")
                        .param("username", "testuser")
                        .param("password", "testpassword")).andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/homePage"))
                .andExpect(request().sessionAttribute("loggedInUser", user));
    }

    @Test
    public void loginInvalidCredentialsTest() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "testuser")
                        .param("password", "wrongpassword")).andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Username and password do not match known users")))
                .andExpect(request().sessionAttributeDoesNotExist("loggedInUser"));
    }
}
