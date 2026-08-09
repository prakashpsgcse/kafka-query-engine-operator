package io.github.prakash.kqe.operator.api.service;

import io.github.prakash.kqe.operator.api.config.KqeApiProperties;
import io.github.prakash.kqe.operator.api.model.IndexRequest;
import io.github.prakash.kqe.operator.api.model.MessageRequest;
import io.github.prakash.kqe.operator.api.model.QueryRequest;
import io.github.prakash.kqe.operator.api.utils.QueryEngineRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaQueryService {

    private final QueryEngineRegistry registry;

    private final RestClient restClient = RestClient.create();

    private final KqeApiProperties kqeApiProperties;

    public ResponseEntity<JsonNode> query(QueryRequest request) {
        String endpoint = getEndpoint(request.getTopic(), request.getBootstrapServers());

        log.info("query endpoint {} req {}",endpoint,request);

        String url = UriComponentsBuilder
                .fromUriString(endpoint)
                .path(kqeApiProperties.getBasePath())
                .path(kqeApiProperties.getQueryPath())
                .queryParam("field", request.getField())
                .queryParam("text", request.getQuery())
                .toUriString();

        log.info("Calling KQE endpoint: {}", url);

        return restClient.get()
                .uri(url)
                .retrieve()
                .toEntity(JsonNode.class);
    }

    public ResponseEntity<JsonNode> message(MessageRequest request) {

        String endpoint = getEndpoint(
                request.getTopic(),
                request.getBootstrapServers());

        log.info("message endpoint {} req {}", endpoint, request);

        String url = UriComponentsBuilder
                .fromUriString(endpoint)
                .path(kqeApiProperties.getBasePath())
                .path(kqeApiProperties.getMessagesPath())
                .queryParam("partition", request.getPartition())
                .queryParam("offset", request.getOffset())
                .toUriString();

        log.info("Calling KQE endpoint: {}", url);

        return restClient.get()
                .uri(url)
                .retrieve()
                .toEntity(JsonNode.class);
    }

    public ResponseEntity<JsonNode> indexStatus(IndexRequest request) {

        String endpoint = getEndpoint(
                request.getTopic(),
                request.getBootstrapServers());

        log.info("index status endpoint {} req {}", endpoint, request);

        String url = UriComponentsBuilder
                .fromUriString(endpoint)
                .path(kqeApiProperties.getBasePath())
                .path(kqeApiProperties.getIndexStatusPath())
                .toUriString();

        log.info("Calling KQE endpoint: {}", url);

        return restClient.get()
                .uri(url)
                .retrieve()
                .toEntity(JsonNode.class);
    }

    public ResponseEntity<JsonNode> deleteIndex(IndexRequest request) {

        String endpoint = getEndpoint(
                request.getTopic(),
                request.getBootstrapServers());

        log.info("delete index endpoint {} req {}", endpoint, request);

        String url = UriComponentsBuilder
                .fromUriString(endpoint)
                .path(kqeApiProperties.getBasePath())
                .path(kqeApiProperties.getIndexPath())
                .toUriString();

        log.info("Calling KQE endpoint: {}", url);

        return restClient.delete()
                .uri(url)
                .retrieve()
                .toEntity(JsonNode.class);
    }

    private String getEndpoint(String topic, String bootstrapServers) {

        log.info(" Endpoint registry {}",registry.getEndpoints());
        return registry.getEndpoint(bootstrapServers,topic);
    }
}