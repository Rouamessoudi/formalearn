package com.esprit.formation;

import com.esprit.formation.config.DemoProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class IntegrationSupport {

    @DynamicPropertySource
    static void demoPasswordsFromEnv(DynamicPropertyRegistry registry) {
        loadOptionalDotEnv(Path.of(".env"));
        loadOptionalDotEnv(Path.of("..", ".env"));
        String admin = firstNonBlank(System.getenv("DEMO_ADMIN_PASSWORD"), System.getProperty("DEMO_ADMIN_PASSWORD"));
        String learner = firstNonBlank(System.getenv("DEMO_LEARNER_PASSWORD"), System.getProperty("DEMO_LEARNER_PASSWORD"));
        if (admin == null || learner == null) {
            throw new IllegalStateException(
                    "Set DEMO_ADMIN_PASSWORD and DEMO_LEARNER_PASSWORD (Jenkins credentials or local .env).");
        }
        registry.add("app.demo.admin-password", () -> admin);
        registry.add("app.demo.learner-password", () -> learner);
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a;
        }
        if (b != null && !b.isBlank()) {
            return b;
        }
        return null;
    }

    private static void loadOptionalDotEnv(Path path) {
        if (!Files.isRegularFile(path)) {
            return;
        }
        try {
            List<String> lines = Files.readAllLines(path);
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#") || !trimmed.contains("=")) {
                    continue;
                }
                int eq = trimmed.indexOf('=');
                String key = trimmed.substring(0, eq).trim();
                String value = trimmed.substring(eq + 1).trim();
                if (System.getenv(key) == null && System.getProperty(key) == null) {
                    System.setProperty(key, value);
                }
            }
        } catch (IOException ignored) {
            // Local helper only; CI injects env vars.
        }
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected DemoProperties demo;

    protected String login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsByteArray());
        return json.get("token").asText();
    }

    protected String adminToken() throws Exception {
        return login(demo.getAdminEmail(), demo.getAdminPassword());
    }

    protected String learnerToken() throws Exception {
        return login(demo.getLearnerEmail(), demo.getLearnerPassword());
    }

    protected String learner2Token() throws Exception {
        return login(demo.getLearner2Email(), demo.getLearnerPassword());
    }
}
