package com.example.demo.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.security.JwtService;

@SpringBootTest
@AutoConfigureMockMvc
class ProjectApiControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JwtService jwtService;

    @Test
    void 未認証では401になること() throws Exception {
        mockMvc.perform(get("/api/projects"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void 有効なBearerトークンなら200になること() throws Exception {
        String token = jwtService.generateToken("demo");
        mockMvc.perform(get("/api/projects").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }
}
