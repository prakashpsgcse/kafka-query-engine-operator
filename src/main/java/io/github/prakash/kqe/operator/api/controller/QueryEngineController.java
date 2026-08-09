package io.github.prakash.kqe.operator.api.controller;

import io.github.prakash.kqe.operator.api.model.QueryEngineRequest;
import io.github.prakash.kqe.operator.api.model.QueryEngineResponse;
import io.github.prakash.kqe.operator.api.model.UpdateTtlRequest;
import io.github.prakash.kqe.operator.api.service.QueryEngineService;
import io.github.prakash.kqe.operator.kubernetes.crd.KafkaQueryEngine;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/query-engines")
@Validated
public class QueryEngineController {
    @Autowired
    private QueryEngineService queryEngineService;

    @PostMapping
    public ResponseEntity<QueryEngineResponse> create(
            @Valid @RequestBody QueryEngineRequest request) {

        QueryEngineResponse response = queryEngineService.create(request);

        return ResponseEntity
                .created(URI.create("/api/v1/query-engines/" + response.getEngineId()))
                .body(response);
    }

    @GetMapping("/{name}")
    public QueryEngineResponse get(@PathVariable String name) {
        return queryEngineService.get(name);
    }

    @GetMapping
    public List<QueryEngineResponse> list() {
        return queryEngineService.list();
    }

    @PatchMapping("/{name}/ttl")
    public QueryEngineResponse updateTtl(
            @PathVariable String name,
            @Valid @RequestBody UpdateTtlRequest request) {
        return queryEngineService.updateTtl(name, request.ttl());
    }

    @DeleteMapping("/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable String name) {
        queryEngineService.delete(name);
    }

}
