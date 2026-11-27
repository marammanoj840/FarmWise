package com.farmwise.backend;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerLoginRefreshVerifyAndResetPasswordFlow() throws Exception {
        String registerPayload = """
                {
                  "fullName": "Rajesh Patel",
                  "email": "rajesh.patel@example.in",
                  "password": "StrongPassword#2026",
                  "phone": "+91-9876543210",
                  "role": "FARMER",
                  "city": "Anand",
                  "state": "Gujarat"
                }
                """;

        MvcResult regResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.user.primaryRole").value("FARMER"))
                .andExpect(jsonPath("$.user.emailVerified").value(false))
                .andReturn();

        JsonNode regJson = objectMapper.readTree(regResult.getResponse().getContentAsString());
        String accessToken = regJson.get("accessToken").asText();
        String refreshToken = regJson.get("refreshToken").asText();
        String verifyToken = regJson.get("devEmailVerificationToken").asText();

        // /me succeeds with valid bearer JWT
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("rajesh.patel@example.in"));

        // Verify email
        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + verifyToken + "\"}"))
                .andExpect(status().isOk());

        // Refresh rotation issues new pair and invalidates old refresh token
        MvcResult refreshRes = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()))
                .andReturn();

        String newRefresh = objectMapper.readTree(refreshRes.getResponse().getContentAsString())
                .get("refreshToken").asText();

        // Replaying old refresh token must fail
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isUnauthorized());

        // Forgot + reset password
        MvcResult forgotRes = mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"rajesh.patel@example.in\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String resetToken = objectMapper.readTree(forgotRes.getResponse().getContentAsString())
                .get("devResetTokenPreview").asText();

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + resetToken + "\",\"newPassword\":\"UpdatedPass#2026\"}"))
                .andExpect(status().isOk());

        // Refresh token revoked after password reset
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + newRefresh + "\"}"))
                .andExpect(status().isUnauthorized());

        // Login with new password succeeds; old password fails
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"rajesh.patel@example.in\",\"password\":\"StrongPassword#2026\"}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"rajesh.patel@example.in\",\"password\":\"UpdatedPass#2026\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.emailVerified").value(true));
    }

    @Test
    void selfRegistrationRejectsAdminRole() throws Exception {
        String payload = """
                {
                  "fullName": "Unauthorized Admin",
                  "email": "fake.admin@example.in",
                  "password": "StrongPassword#2026",
                  "role": "ADMIN"
                }
                """;
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("business_validation_error"));
    }
}
