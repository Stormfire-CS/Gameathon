package edu.carroll.cs341.web.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.carroll.cs341.jpa.model.User;
import edu.carroll.cs341.service.FamilyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CreateFamilyController.class)
@AutoConfigureRestTestClient
public class CreateFamilyControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FamilyService familyService;

    @Test
    public void createFamilyPageTest() throws Exception {
        User user = new User("testuser", "testpassword");

        mockMvc.perform(get("/createFamilyPage").sessionAttr("loggedInUser", user)).andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Create a Family")))
                .andExpect(content().string(containsString("action=\"/createFamily\"")));
    }

    @Test
    public void createFamilyTest() throws Exception {
        User user = new User("testuser", "testpassword");
        when(familyService.createFamily(user, "TestFamily")).thenReturn(true);

        mockMvc.perform(post("/createFamily")
                        .sessionAttr("loggedInUser", user)
                        .param("familyName", "TestFamily")).andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/familyPage"));

        verify(familyService).createFamily(user, "TestFamily");
    }
}
