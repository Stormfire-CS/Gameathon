package edu.carroll.cs341.web.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.carroll.cs341.jpa.model.Game;
import edu.carroll.cs341.jpa.model.GameOwned;
import edu.carroll.cs341.jpa.model.User;
import edu.carroll.cs341.service.GamesOwnedService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ViewGamesController.class)
@AutoConfigureRestTestClient
public class ViewGamesControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GamesOwnedService gamesOwnedService;

    @Test
    public void viewGamesPageTest() throws Exception {
        User user = new User("testuser", "testpassword");
        GameOwned ownedGame = new GameOwned(new Game("Chess"), user);
        when(gamesOwnedService.getGamesOwnedByUsername(user)).thenReturn(List.of(ownedGame));

        mockMvc.perform(get("/viewGamesPage").sessionAttr("loggedInUser", user)).andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("View Games")))
                .andExpect(content().string(containsString("Chess")));
    }

    @Test
    public void viewGamesPageRequiresLoginTest() throws Exception {
        mockMvc.perform(get("/viewGamesPage")).andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}
