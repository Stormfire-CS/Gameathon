package edu.carroll.cs341.web.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.carroll.cs341.jpa.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(LogoutController.class)
@AutoConfigureRestTestClient
public class LogoutControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void logoutTest() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", new User("testuser", "testpassword"));

        mockMvc.perform(post("/logout").session(session)).andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        assertTrue(session.isInvalid(), "Logout should invalidate the session");
    }
}
