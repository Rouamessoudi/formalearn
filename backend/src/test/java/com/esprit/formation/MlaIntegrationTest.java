package com.esprit.formation;

import com.esprit.formation.mla.MlScoringClient;
import com.esprit.formation.mla.dto.MlPredictRequest;
import com.esprit.formation.mla.dto.MlScoredFormation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MlaIntegrationTest extends IntegrationSupport {

    @MockBean
    private MlScoringClient mlScoringClient;

    @Test
    void unauthenticatedRecommendationsReturn401() throws Exception {
        mockMvc.perform(get("/api/mla/recommandations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void learnerRecommendationsUseProfileAndOnlyPublishedFormations() throws Exception {
        String learner = learnerToken();
        String admin = adminToken();

        when(mlScoringClient.score(any())).thenAnswer(invocation -> {
            MlPredictRequest request = invocation.getArgument(0);
            return request.getFormations().stream().map(item -> {
                MlScoredFormation scored = new MlScoredFormation();
                scored.setFormationId(item.getFormationId());
                scored.setScore(item.getFormationCategory().equals("INFORMATIQUE") ? 0.91 : 0.21);
                return scored;
            }).toList();
        });

        MvcResult published = mockMvc.perform(get("/api/formations").header("Authorization", "Bearer " + learner))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode publishedList = objectMapper.readTree(published.getResponse().getContentAsByteArray());
        assertThat(publishedList).isNotEmpty();
        for (JsonNode node : publishedList) {
            assertThat(node.get("status").asText()).isEqualTo("PUBLISHED");
        }

        mockMvc.perform(get("/api/mla/recommandations").header("Authorization", "Bearer " + learner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].score").exists())
                .andExpect(jsonPath("$[0].title").exists())
                .andExpect(jsonPath("$[0].status").value("PUBLISHED"));

        ArgumentCaptor<MlPredictRequest> captor = ArgumentCaptor.forClass(MlPredictRequest.class);
        verify(mlScoringClient).score(captor.capture());
        MlPredictRequest sent = captor.getValue();
        assertThat(sent.getProfile().getInterest()).isEqualTo("BACKEND");
        assertThat(sent.getProfile().getHasJava()).isEqualTo(1);
        assertThat(sent.getProfile().getHasSpring()).isEqualTo(1);
        assertThat(sent.getFormations()).hasSize(publishedList.size());
        assertThat(sent.getFormations()).extracting("formationId")
                .containsExactlyInAnyOrderElementsOf(
                        java.util.stream.StreamSupport.stream(publishedList.spliterator(), false)
                                .map(node -> node.get("id").asLong())
                                .toList()
                );

        mockMvc.perform(get("/api/formations").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.status=='DRAFT')]").isNotEmpty());
    }

    @Test
    void updatedProfileIsSentToMlService() throws Exception {
        String learner = learnerToken();
        mockMvc.perform(put("/api/profil")
                        .header("Authorization", "Bearer " + learner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"interest":"DATA","experienceYears":4,"educationLevel":"MASTER",
                                "skills":["PYTHON","SQL"]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.interest").value("DATA"));
        try {
            when(mlScoringClient.score(any())).thenReturn(List.of());
            mockMvc.perform(get("/api/mla/recommandations").header("Authorization", "Bearer " + learner))
                    .andExpect(status().isOk());

            ArgumentCaptor<MlPredictRequest> captor = ArgumentCaptor.forClass(MlPredictRequest.class);
            verify(mlScoringClient).score(captor.capture());
            assertThat(captor.getValue().getProfile().getInterest()).isEqualTo("DATA");
            assertThat(captor.getValue().getProfile().getHasPython()).isEqualTo(1);
            assertThat(captor.getValue().getProfile().getHasJava()).isEqualTo(0);
        } finally {
            mockMvc.perform(put("/api/profil")
                            .header("Authorization", "Bearer " + learner)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"interest":"BACKEND","experienceYears":3,"educationLevel":"INGENIEUR",
                                    "skills":["JAVA","SPRING","SQL"]}
                                    """))
                    .andExpect(status().isOk());
        }
    }
}
