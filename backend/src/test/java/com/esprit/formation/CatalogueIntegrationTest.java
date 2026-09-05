package com.esprit.formation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CatalogueIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void adminCanCreateCategory() throws Exception {
        String token = login("admin@formalearn.tn", "Admin123!");
        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"DevOps " + System.currentTimeMillis() + "\",\"description\":\"CI/CD\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(startsWith("DevOps")));
    }

    @Test
    void learnerCannotCreateCategory() throws Exception {
        String token = login("apprenant@formalearn.tn", "Learner123!");
        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Hack\",\"description\":\"no\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateFormation() throws Exception {
        String token = login("admin@formalearn.tn", "Admin123!");
        long categoryId = firstCategoryId(token);
        mockMvc.perform(post("/api/formations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Docker essentiels %d","description":"Images et compose",
                                "price":199.00,"durationHours":12,"categoryId":%d,"status":"PUBLISHED"}
                                """.formatted(System.currentTimeMillis(), categoryId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(startsWith("Docker essentiels")))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    @Test
    void searchFormationsByQuery() throws Exception {
        String token = login("apprenant@formalearn.tn", "Learner123!");
        mockMvc.perform(get("/api/formations").param("q", "Spring")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title", hasItem("Spring Boot — Développement d'API REST")));
    }

    @Test
    void learnerDoesNotSeeDraftFormations() throws Exception {
        String learner = login("apprenant@formalearn.tn", "Learner123!");
        String admin = login("admin@formalearn.tn", "Admin123!");
        mockMvc.perform(get("/api/formations").header("Authorization", "Bearer " + learner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.status=='DRAFT')]", hasSize(0)));
        mockMvc.perform(get("/api/formations").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.status=='DRAFT')]", hasSize(greaterThan(0))));
    }

    @Test
    void chaptersAreReturnedInPositionOrder() throws Exception {
        String token = login("apprenant@formalearn.tn", "Learner123!");
        MvcResult listed = mockMvc.perform(get("/api/formations").param("q", "Angular")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        long formationId = objectMapper.readTree(listed.getResponse().getContentAsByteArray()).get(0).get("id").asLong();
        mockMvc.perform(get("/api/formations/{id}/chapitres", formationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].position").value(1))
                .andExpect(jsonPath("$[1].position").value(2));
    }

    @Test
    void unknownFormationReturns404() throws Exception {
        String token = login("apprenant@formalearn.tn", "Learner123!");
        mockMvc.perform(get("/api/formations/{id}", 9_999_999L)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminCanCreateChapterButLearnerCannot() throws Exception {
        String admin = login("admin@formalearn.tn", "Admin123!");
        String learner = login("apprenant@formalearn.tn", "Learner123!");
        MvcResult listed = mockMvc.perform(get("/api/formations").param("q", "TypeScript de zéro")
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andReturn();
        long formationId = objectMapper.readTree(listed.getResponse().getContentAsByteArray()).get(0).get("id").asLong();
        mockMvc.perform(post("/api/formations/{id}/chapitres", formationId)
                        .header("Authorization", "Bearer " + learner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Hack\",\"content\":\"no\",\"position\":" + System.currentTimeMillis() % 100000 + "}"))
                .andExpect(status().isForbidden());
        long pos = 800000 + System.currentTimeMillis() % 10000;
        mockMvc.perform(post("/api/formations/{id}/chapitres", formationId)
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Sécurité JWT\",\"content\":\"Filtres et rôles.\",\"position\":" + pos + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Sécurité JWT"))
                .andExpect(jsonPath("$.position").value(pos));
    }

    @Test
    void adminCanReorderChapters() throws Exception {
        String admin = login("admin@formalearn.tn", "Admin123!");
        MvcResult listed = mockMvc.perform(get("/api/formations").param("q", "TypeScript de zéro")
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andReturn();
        long formationId = objectMapper.readTree(listed.getResponse().getContentAsByteArray()).get(0).get("id").asLong();
        MvcResult chapters = mockMvc.perform(get("/api/formations/{id}/chapitres", formationId)
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andReturn();
        var array = objectMapper.readTree(chapters.getResponse().getContentAsByteArray());
        long firstId = array.get(0).get("id").asLong();
        String firstTitle = array.get(0).get("title").asText();
        mockMvc.perform(patch("/api/formations/{id}/chapitres/{chapterId}/position", formationId, firstId)
                        .param("direction", "down")
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1].title").value(firstTitle));
    }

    private long firstCategoryId(String token) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/categories").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsByteArray()).get(0).get("id").asLong();
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
