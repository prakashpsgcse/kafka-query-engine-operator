package io.github.prakash.kqe.operator.api.controller;

import io.github.prakash.kqe.operator.api.model.IndexRequest;
import io.github.prakash.kqe.operator.api.model.MessageRequest;
import io.github.prakash.kqe.operator.api.model.QueryRequest;
import io.github.prakash.kqe.operator.api.service.KafkaQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api/v1/query-engines")
@RequiredArgsConstructor
public class QueryController {

    private final KafkaQueryService kafkaQueryService;

    @PostMapping("/query")
    public ResponseEntity<JsonNode> query(
            @Valid @RequestBody QueryRequest request) {

        return kafkaQueryService.query(request);
    }

    @PostMapping("/messages")
    public ResponseEntity<JsonNode> message(
            @Valid @RequestBody MessageRequest request) {

        return kafkaQueryService.message(request);
    }

    @PostMapping("/index/status")
    public ResponseEntity<JsonNode> indexStatus(
            @Valid @RequestBody IndexRequest request) {

        return kafkaQueryService.indexStatus(request);
    }

    @DeleteMapping("/index")
    public ResponseEntity<JsonNode> deleteIndex(
            @Valid @RequestBody IndexRequest request) {

        return kafkaQueryService.deleteIndex(request);
    }
}