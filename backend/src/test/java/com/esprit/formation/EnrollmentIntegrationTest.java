package com.esprit.formation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EnrollmentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void unauthenticatedEnrollmentReturns401() throws Exception {
        mockMvc.perform(post("/api/inscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionId\":1}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void learnerCanEnrollAndDuplicateIsRejected() throws Exception {
        String admin = login("admin@formalearn.tn", "Admin123!");
        String learner = login("apprenant@formalearn.tn", "Learner123!");
        long sessionId = createOpenSession(admin, 8);

        mockMvc.perform(post("/api/inscriptions")
                        .header("Authorization", "Bearer " + learner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionId\":" + sessionId + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.sessionId").value((int) sessionId));

        mockMvc.perform(post("/api/inscriptions")
                        .header("Authorization", "Bearer " + learner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionId\":" + sessionId + "}"))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/sessions/{id}", sessionId).header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enrolledCount").value(1))
                .andExpect(jsonPath("$.remainingPlaces").value(7));
    }

    @Test
    void fullSessionIsRejected() throws Exception {
        String admin = login("admin@formalearn.tn", "Admin123!");
        String learner = login("apprenant@formalearn.tn", "Learner123!");
        String other = login("apprenant2@formalearn.tn", "Learner123!");
        long sessionId = createOpenSession(admin, 1);

        mockMvc.perform(post("/api/inscriptions")
                        .header("Authorization", "Bearer " + learner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionId\":" + sessionId + "}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/inscriptions")
                        .header("Authorization", "Bearer " + other)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionId\":" + sessionId + "}"))
                .andExpect(status().isConflict());
    }

    @Test
    void closedSessionIsRejected() throws Exception {
        String admin = login("admin@formalearn.tn", "Admin123!");
        String learner = login("apprenant@formalearn.tn", "Learner123!");
        long formationId = publishedFormationId(admin);
        LocalDate start = LocalDate.now(ZoneOffset.UTC).plusDays(20);
        long sessionId = createSession(admin, formationId, start, start.plusDays(5), 10, "CLOSED");

        mockMvc.perform(post("/api/inscriptions")
                        .header("Authorization", "Bearer " + learner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionId\":" + sessionId + "}"))
                .andExpect(status().isConflict());
    }

    @Test
    void learnerSeesOnlyOwnEnrollmentsWhileAdminSeesSessionList() throws Exception {
        String admin = login("admin@formalearn.tn", "Admin123!");
        String learner = login("apprenant@formalearn.tn", "Learner123!");
        String other = login("apprenant2@formalearn.tn", "Learner123!");
        long sessionId = createOpenSession(admin, 10);

        mockMvc.perform(post("/api/inscriptions")
                        .header("Authorization", "Bearer " + learner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionId\":" + sessionId + "}"))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/inscriptions")
                        .header("Authorization", "Bearer " + other)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionId\":" + sessionId + "}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/inscriptions/moi").header("Authorization", "Bearer " + learner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.learnerEmail=='apprenant2@formalearn.tn')]", hasSize(0)))
                .andExpect(jsonPath("$[?(@.sessionId==" + sessionId + " && @.learnerEmail=='apprenant@formalearn.tn')]", hasSize(1)));

        mockMvc.perform(get("/api/sessions/{id}/inscriptions", sessionId).header("Authorization", "Bearer " + other))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/sessions/{id}/inscriptions", sessionId).header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void cancellationFreesASeat() throws Exception {
        String admin = login("admin@formalearn.tn", "Admin123!");
        String learner = login("apprenant@formalearn.tn", "Learner123!");
        String other = login("apprenant2@formalearn.tn", "Learner123!");
        long sessionId = createOpenSession(admin, 1);

        MvcResult created = mockMvc.perform(post("/api/inscriptions")
                        .header("Authorization", "Bearer " + learner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionId\":" + sessionId + "}"))
                .andExpect(status().isCreated())
                .andReturn();
        long enrollmentId = objectMapper.readTree(created.getResponse().getContentAsByteArray()).get("id").asLong();

        mockMvc.perform(get("/api/sessions/{id}", sessionId).header("Authorization", "Bearer " + admin))
                .andExpect(jsonPath("$.remainingPlaces").value(0));

        mockMvc.perform(patch("/api/inscriptions/{id}/status", enrollmentId)
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CANCELLED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        mockMvc.perform(get("/api/sessions/{id}", sessionId).header("Authorization", "Bearer " + admin))
                .andExpect(jsonPath("$.enrolledCount").value(0))
                .andExpect(jsonPath("$.remainingPlaces").value(1));

        mockMvc.perform(post("/api/inscriptions")
                        .header("Authorization", "Bearer " + other)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionId\":" + sessionId + "}"))
                .andExpect(status().isCreated());
    }

    @Test
    void learnerCanCancelOwnEnrollmentAndFreeSeat() throws Exception {
        String admin = login("admin@formalearn.tn", "Admin123!");
        String learner = login("apprenant@formalearn.tn", "Learner123!");
        String other = login("apprenant2@formalearn.tn", "Learner123!");
        long sessionId = createOpenSession(admin, 1);

        MvcResult created = mockMvc.perform(post("/api/inscriptions")
                        .header("Authorization", "Bearer " + learner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionId\":" + sessionId + "}"))
                .andExpect(status().isCreated())
                .andReturn();
        long enrollmentId = objectMapper.readTree(created.getResponse().getContentAsByteArray()).get("id").asLong();

        mockMvc.perform(post("/api/inscriptions/{id}/annuler", enrollmentId)
                        .header("Authorization", "Bearer " + other))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/inscriptions/{id}/annuler", enrollmentId)
                        .header("Authorization", "Bearer " + learner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.remainingPlaces").value(1));
    }

    @Test
    void adminCanUpdateAndDeleteSessionWithoutEnrollments() throws Exception {
        String admin = login("admin@formalearn.tn", "Admin123!");
        long formationId = publishedFormationId(admin);
        LocalDate start = LocalDate.now(ZoneOffset.UTC).plusDays(40);
        long sessionId = createSession(admin, formationId, start, start.plusDays(5), 10, "OPEN");

        mockMvc.perform(put("/api/sessions/{id}", sessionId)
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"formationId":%d,"startDate":"%s","endDate":"%s","capacity":8,"status":"CLOSED"}
                                """.formatted(formationId, start, start.plusDays(6))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capacity").value(8))
                .andExpect(jsonPath("$.status").value("CLOSED"));

        mockMvc.perform(delete("/api/sessions/{id}", sessionId)
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/sessions/{id}", sessionId).header("Authorization", "Bearer " + admin))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidSessionDatesReturn400() throws Exception {
        String admin = login("admin@formalearn.tn", "Admin123!");
        long formationId = publishedFormationId(admin);
        LocalDate start = LocalDate.now(ZoneOffset.UTC).plusDays(50);
        mockMvc.perform(post("/api/sessions")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"formationId":%d,"startDate":"%s","endDate":"%s","capacity":10,"status":"OPEN"}
                                """.formatted(formationId, start, start.minusDays(1))))
                .andExpect(status().isBadRequest());
    }

    private long createOpenSession(String adminToken, int capacity) throws Exception {
        LocalDate start = LocalDate.now(ZoneOffset.UTC).plusDays(30);
        return createSession(adminToken, publishedFormationId(adminToken), start, start.plusDays(5), capacity, "OPEN");
    }

    private long createSession(
            String adminToken,
            long formationId,
            LocalDate start,
            LocalDate end,
            int capacity,
            String status
    ) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/sessions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"formationId":%d,"startDate":"%s","endDate":"%s","capacity":%d,"status":"%s"}
                                """.formatted(formationId, start, end, capacity, status)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsByteArray()).get("id").asLong();
    }

    private long publishedFormationId(String adminToken) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/formations").param("q", "Spring")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode list = objectMapper.readTree(result.getResponse().getContentAsByteArray());
        for (JsonNode node : list) {
            if ("PUBLISHED".equals(node.get("status").asText()) && node.get("title").asText().toLowerCase().contains("spring")) {
                return node.get("id").asLong();
            }
        }
        throw new IllegalStateException("Aucune formation Spring publiée");
    }

    private String login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsByteArray()).get("token").asText();
    }
}
