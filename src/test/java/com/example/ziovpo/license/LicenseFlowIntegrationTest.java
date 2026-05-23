package com.example.ziovpo.license;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.ziovpo.license.config.DemoIds;
import com.example.ziovpo.license.model.LicenseLicenseType;
import com.example.ziovpo.license.model.LicenseProduct;
import com.example.ziovpo.license.repository.LicenseTypeRepository;
import com.example.ziovpo.license.repository.LicenseProductRepository;
import com.example.ziovpo.license.service.TicketSignatureService;
import com.example.ziovpo.model.Users;
import com.example.ziovpo.repository.UsersRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LicenseFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private LicenseProductRepository productRepository;

    @Autowired
    private LicenseTypeRepository licenseTypeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TicketSignatureService ticketSignatureService;

    @BeforeEach
    void seed() {
        if (usersRepository.findByUsername("admin").isEmpty()) {
            Users admin = new Users();
            admin.setId(DemoIds.ADMIN_ID);
            admin.setUsername("admin");
            admin.setEmail("admin@test.com");
            admin.setPassword(passwordEncoder.encode("Admin1234!"));
            admin.setRole("ROLE_ADMIN");
            usersRepository.save(admin);
        }
        if (usersRepository.findByUsername("client").isEmpty()) {
            Users client = new Users();
            client.setId(DemoIds.CLIENT_ID);
            client.setUsername("client");
            client.setEmail("client@test.com");
            client.setPassword(passwordEncoder.encode("Client1234!"));
            client.setRole("ROLE_USER");
            usersRepository.save(client);
        }
        if (!productRepository.existsById(DemoIds.PRODUCT_ID)) {
            LicenseProduct p = new LicenseProduct();
            p.setId(DemoIds.PRODUCT_ID);
            p.setName("Car Service Desktop");
            productRepository.save(p);
        }
        if (!licenseTypeRepository.existsById(DemoIds.TYPE_STANDARD_ID)) {
            LicenseLicenseType t = new LicenseLicenseType();
            t.setId(DemoIds.TYPE_STANDARD_ID);
            t.setName("STANDARD");
            t.setDefaultDurationInDays(30);
            licenseTypeRepository.save(t);
        }
    }

    @Test
    void fullFlowWithSignedTicket() throws Exception {
        String adminToken = login("admin", "Admin1234!");

        String createBody = """
                {
                  "productId": "%s",
                  "typeId": "%s",
                  "ownerId": "%s",
                  "deviceCount": 2,
                  "description": "lab3 test"
                }
                """.formatted(DemoIds.PRODUCT_ID, DemoIds.TYPE_STANDARD_ID, DemoIds.CLIENT_ID);

        MvcResult createResult = mockMvc.perform(post("/api/licenses")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        String code = created.has("activationKey")
                ? created.get("activationKey").asText()
                : created.get("code").asText();

        String clientToken = login("client", "Client1234!");

        String activateBody = """
                {
                  "activationKey": "%s",
                  "deviceMac": "AA:BB:CC:DD:EE:01",
                  "deviceName": "PC-1"
                }
                """.formatted(code);

        MvcResult activateResult = mockMvc.perform(post("/api/licenses/activate")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(activateBody))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode activated = objectMapper.readTree(activateResult.getResponse().getContentAsString());
        assertNotNull(activated.get("ticket"));
        assertNotNull(activated.get("signature"));
        assertTrue(ticketSignatureService.verifyTicketSignature(
                objectMapper.treeToValue(activated.get("ticket"), com.example.ziovpo.license.dto.Ticket.class),
                activated.get("signature").asText()));

        String checkBody = """
                {
                  "deviceMac": "AA:BB:CC:DD:EE:01",
                  "productId": "%s"
                }
                """.formatted(DemoIds.PRODUCT_ID);

        MvcResult checkResult = mockMvc.perform(post("/api/licenses/check")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkBody))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode checked = objectMapper.readTree(checkResult.getResponse().getContentAsString());
        assertFalse(checked.get("ticket").get("blocked").asBoolean());
    }

    private String login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }
}
