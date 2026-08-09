package io.github.prakash.kqe.operator.api.utils;

import io.github.prakash.kqe.operator.api.utils.EngineIdGenerator;
import io.github.prakash.kqe.operator.kubernetes.crd.KafkaQueryEngine;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class QueryEngineRegistry {

    private final ConcurrentHashMap<String, String> endpoints =
            new ConcurrentHashMap<>();

    public void register(KafkaQueryEngine resource) {

        String engineId = EngineIdGenerator.generate(
                resource.getSpec().getBootstrapServers(),
                resource.getSpec().getTopic());

        endpoints.put(engineId,
                resource.getStatus().getServiceEndpoint());
    }

    public String getEndpoint(String bootstrapServers, String topic) {
        String engineId = EngineIdGenerator.generate(bootstrapServers, topic);
        return endpoints.get(engineId);
    }

    public void unregister(KafkaQueryEngine resource) {

        String engineId = EngineIdGenerator.generate(
                resource.getSpec().getBootstrapServers(),
                resource.getSpec().getTopic());

        endpoints.remove(engineId);
    }

    public ConcurrentHashMap<String, String> getEndpoints(){
        return endpoints;
    }
}
