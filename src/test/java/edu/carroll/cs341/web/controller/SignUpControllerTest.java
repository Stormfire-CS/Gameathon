package edu.carroll.cs341.web.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
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

@WebMvcTest(SignUpController.class)
@AutoConfigureRestTestClient
public class SignUpControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    public void signUpPageTest() throws Exception {
        mockMvc.perform(get("/signUpPage")).andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<h1>Sign Up</h1>")))
                .andExpect(content().string(containsString("action=\"/signUpPage\"")));
    }

    @Test
    public void signUpSuccessTest() throws Exception {
        User user = new User("testuser", "testpassword");
        when(userService.newUser("testuser", "testpassword")).thenReturn(user);

        mockMvc.perform(post("/signUpPage")
                        .param("username", "testuser")
                        .param("password1", "testpassword")
                        .param("password2", "testpassword")).andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/homePage"))
                .andExpect(request().sessionAttribute("loggedInUser", user));
    }

    @Test
    public void signUpMismatchedPasswordsTest() throws Exception {
        mockMvc.perform(post("/signUpPage")
                        .param("username", "testuser")
                        .param("password1", "testpassword")
                        .param("password2", "otherpassword")).andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("createUserForm", "passwordMatching"))
                .andExpect(request().sessionAttributeDoesNotExist("loggedInUser"));
    }
}
