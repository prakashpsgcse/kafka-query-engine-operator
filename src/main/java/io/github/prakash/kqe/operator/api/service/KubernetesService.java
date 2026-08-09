package io.github.prakash.kqe.operator.api.service;

import io.fabric8.kubernetes.api.model.StatusDetails;
import io.github.prakash.kqe.operator.kubernetes.crd.KafkaQueryEngine;

import java.util.List;
import java.util.Optional;

public interface KubernetesService {

    KafkaQueryEngine create(KafkaQueryEngine resource);

    Optional<KafkaQueryEngine> get(String namespace, String name);

    List<KafkaQueryEngine> list(String namespace);

    KafkaQueryEngine update(KafkaQueryEngine resource);

    boolean delete(String namespace, String name);

    KafkaQueryEngine findByTopicAndBootstrapServers(
            String topic,
            String bootstrapServers);
}
