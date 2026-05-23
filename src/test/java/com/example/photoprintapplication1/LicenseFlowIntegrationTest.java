package com.example.photoprintapplication1;

import com.example.photoprintapplication1.dto.AuthRequest;
import com.example.photoprintapplication1.dto.AuthResponse;
import com.example.photoprintapplication1.dto.TicketResponse;
import com.example.photoprintapplication1.service.SignatureService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LicenseFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SignatureService signatureService;

    @Test
    void fullLicenseLifecycle() throws Exception {
        String adminToken = login("admin", "Admin1234!");

        String licenseJson = """
                {"productId":1,"typeId":1,"deviceCount":2,"description":"demo"}
                """;
        MvcResult createResult = mockMvc.perform(post("/api/license")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(licenseJson))
                .andExpect(status().isCreated())
                .andReturn();

        @SuppressWarnings("unchecked")
        Map<String, Object> license = objectMapper.readValue(createResult.getResponse().getContentAsString(), Map.class);
        String code = (String) license.get("code");
        assertNotNull(code);

        String userToken = login("admin", "Admin1234!");

        String activateJson = """
                {"activationKey":"%s","deviceMac":"AA:BB:CC:11:22:33","deviceName":"Workstation-1"}
                """.formatted(code);

        MvcResult activateResult = mockMvc.perform(post("/api/license/activate")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(activateJson))
                .andExpect(status().isOk())
                .andReturn();

        TicketResponse activated = objectMapper.readValue(
                activateResult.getResponse().getContentAsString(), TicketResponse.class);
        assertNotNull(activated.getTicket().getServerTime());
        assertTrue(activated.getTicket().getTicketLifetimeSeconds() > 0);
        assertNotNull(activated.getTicket().getFirstActivationDate());
        assertNotNull(activated.getTicket().getExpirationDate());
        assertNotNull(activated.getTicket().getUserId());
        assertNotNull(activated.getTicket().getDeviceId());
        assertFalse(activated.getTicket().isBlocked());
        assertTrue(signatureService.verifyTicket(activated.getTicket(), activated.getSignature()));

        String checkJson = """
                {"deviceMac":"AA:BB:CC:11:22:33"}
                """;
        MvcResult checkResult = mockMvc.perform(post("/api/license/check")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkJson))
                .andExpect(status().isOk())
                .andReturn();

        TicketResponse checked = objectMapper.readValue(
                checkResult.getResponse().getContentAsString(), TicketResponse.class);
        assertEquals(activated.getTicket().getUserId(), checked.getTicket().getUserId());
        assertTrue(signatureService.verifyTicket(checked.getTicket(), checked.getSignature()));
    }

    private String login(String username, String password) throws Exception {
        AuthRequest request = new AuthRequest();
        request.setUsername(username);
        request.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), AuthResponse.class);
        assertNotNull(response.getAccessToken());
        return response.getAccessToken();
    }
}
