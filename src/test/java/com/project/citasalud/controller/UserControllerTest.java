package com.project.citasalud.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAllUsers_ShouldReturnAllUsersMessage() throws Exception {
        mockMvc.perform(post("/api/user/all"))
                .andExpect(status().isOk())
                .andExpect(content().string("All users"));
    }
}
