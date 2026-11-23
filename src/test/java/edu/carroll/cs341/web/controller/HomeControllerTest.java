package edu.carroll.cs341.web.controller;

import static org.hamcrest.Matchers.containsString;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.carroll.cs341.jpa.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HomeController.class)
@AutoConfigureRestTestClient
public class HomeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void homePageTest() throws Exception {
        User user = new User();
        user.setName("Test Player");

        mockMvc.perform(get("/homePage").sessionAttr("loggedInUser", user)).andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Gameathon Home")))
                .andExpect(content().string(containsString("Test Player")));
    }

    @Test
    public void homePageRequiresLoginTest() throws Exception {
        mockMvc.perform(get("/homePage")).andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}
