package com.farmwise.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.farmwise.backend.entity.RoleEntity;
import com.farmwise.backend.repository.RoleRepository;
import com.farmwise.backend.user.UserEntity;
import com.farmwise.backend.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityAndOwnershipTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String registerAndGetToken(String email, String role) throws Exception {
        String body = """
                {
                  "fullName": "User %s",
                  "email": "%s",
                  "password": "SecurePassword#99",
                  "role": "%s"
                }
                """.formatted(role, email, role);
        MvcResult res = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(res.getResponse().getContentAsString()).get("accessToken").asText();
    }

    @Test
    void farmerIsolationAndAdminRbacAreEnforced() throws Exception {
        String farmerAToken = registerAndGetToken("farmer.a@example.in", "FARMER");
        String farmerBToken = registerAndGetToken("farmer.b@example.in", "FARMER");
        String expertToken = registerAndGetToken("expert.sec@example.in", "EXPERT");

        // Farmer A creates a farm
        String farmPayload = """
                {
                  "name": "Green Valley Farm",
                  "landArea": 5.5,
                  "areaUnit": "ACRES",
                  "soilType": "Alluvial Loam",
                  "irrigationType": "Drip",
                  "location": {
                    "villageOrCity": "Khanna",
                    "district": "Ludhiana",
                    "state": "Punjab"
                  }
                }
                """;
        MvcResult farmRes = mockMvc.perform(post("/api/v1/farms")
                        .header("Authorization", "Bearer " + farmerAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(farmPayload))
                .andExpect(status().isCreated())
                .andReturn();
        String farmId = objectMapper.readTree(farmRes.getResponse().getContentAsString()).get("id").asText();

        // Farmer B cannot read or delete Farmer A's farm
        mockMvc.perform(get("/api/v1/farms/" + farmId)
                        .header("Authorization", "Bearer " + farmerBToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("forbidden"));

        mockMvc.perform(delete("/api/v1/farms/" + farmId)
                        .header("Authorization", "Bearer " + farmerBToken))
                .andExpect(status().isForbidden());

        // Non-admin cannot access admin endpoints
        mockMvc.perform(get("/api/v1/admin/stats")
                        .header("Authorization", "Bearer " + farmerAToken))
                .andExpect(status().isForbidden());

        // Expert starts unverified until Admin verifies them
        JsonNode expertsList = objectMapper.readTree(
                mockMvc.perform(get("/api/v1/experts")).andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString());
        String expertId = expertsList.get(0).get("id").asText();
        mockMvc.perform(get("/api/v1/experts/" + expertId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(false));

        // Create Admin directly via repository and login
        if (userRepository.findByEmailIgnoreCase("admin.test@example.in").isEmpty()) {
            RoleEntity adminRole = roleRepository.findByName("ADMIN").orElseThrow();
            UserEntity admin = new UserEntity();
            admin.setEmail("admin.test@example.in");
            admin.setFullName("Test Admin");
            admin.setPasswordHash(passwordEncoder.encode("AdminPass#2026"));
            admin.setPrimaryRole("ADMIN");
            admin.setActive(true);
            admin.setEmailVerified(true);
            admin.getRoles().add(adminRole);
            userRepository.save(admin);
        }

        MvcResult adminLogin = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin.test@example.in\",\"password\":\"AdminPass#2026\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String adminToken = objectMapper.readTree(adminLogin.getResponse().getContentAsString())
                .get("accessToken").asText();

        // Admin verifies expert
        mockMvc.perform(patch("/api/v1/admin/experts/" + expertId + "/verify")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"APPROVED\",\"note\":\"Credentials checked\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/experts/" + expertId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(true));

        // Admin audit logs include verification event
        mockMvc.perform(get("/api/v1/admin/audit-logs")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].action").exists());

        // Expert cannot read Farmer A's private farm
        mockMvc.perform(get("/api/v1/farms/" + farmId)
                        .header("Authorization", "Bearer " + expertToken))
                .andExpect(status().isForbidden());
    }
}
