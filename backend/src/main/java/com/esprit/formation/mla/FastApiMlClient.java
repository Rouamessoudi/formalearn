package com.esprit.formation.mla;

import com.esprit.formation.common.exception.ApiException;
import com.esprit.formation.config.MlProperties;
import com.esprit.formation.mla.dto.MlPredictRequest;
import com.esprit.formation.mla.dto.MlPredictResponse;
import com.esprit.formation.mla.dto.MlScoredFormation;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.List;

@Component
public class FastApiMlClient implements MlScoringClient {

    private final RestClient restClient;

    public FastApiMlClient(RestClient.Builder builder, MlProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()));
        factory.setReadTimeout(Duration.ofMillis(properties.getReadTimeoutMs()));
        this.restClient = builder
                .baseUrl(properties.getBaseUrl())
                .requestFactory(factory)
                .build();
    }

    @Override
    public List<MlScoredFormation> score(MlPredictRequest request) {
        try {
            MlPredictResponse response = restClient.post()
                    .uri("/predict")
                    .body(request)
                    .retrieve()
                    .body(MlPredictResponse.class);
            if (response == null || response.getRecommendations() == null) {
                return List.of();
            }
            return response.getRecommendations();
        } catch (ResourceAccessException ex) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Le service de recommandation est indisponible ou a dépassé le délai d'attente");
        } catch (RestClientException ex) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Le service de recommandation a renvoyé une erreur");
        }
    }
}
