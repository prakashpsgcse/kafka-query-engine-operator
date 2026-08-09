package io.github.prakash.kqe.operator.api.service;

import io.fabric8.kubernetes.api.model.StatusDetails;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.github.prakash.kqe.operator.kubernetes.crd.KafkaQueryEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class KubernetesServiceImpl implements KubernetesService {

    @Autowired
    private KubernetesClient kubernetesClient;

    @Override
    public KafkaQueryEngine create(KafkaQueryEngine resource) {
        log.info("Creating KafkaQueryEngineCR {} in namespace {} ",resource.getMetadata().getName(),resource.getMetadata().getNamespace());
        return kubernetesClient.resources(KafkaQueryEngine.class)
                .inNamespace(resource.getMetadata().getNamespace())
                .resource(resource)
                .create();
    }

    @Override
    public Optional<KafkaQueryEngine> get(String namespace, String name) {

        return Optional.ofNullable(
                kubernetesClient.resources(KafkaQueryEngine.class)
                        .inNamespace(namespace)
                        .withName(name)
                        .get()
        );
    }

    @Override
    public List<KafkaQueryEngine> list(String namespace) {
        return kubernetesClient.resources(KafkaQueryEngine.class)
                .inNamespace(namespace)
                .list()
                .getItems();
    }

    @Override
    public KafkaQueryEngine update(KafkaQueryEngine resource) {

        return kubernetesClient.resources(KafkaQueryEngine.class)
                .inNamespace(resource.getMetadata().getNamespace())
                .resource(resource)
                .update();
    }

    @Override
    public boolean delete(String namespace, String name) {
        List<StatusDetails> result = kubernetesClient
                .resources(KafkaQueryEngine.class)
                .inNamespace(namespace)
                .withName(name)
                .delete();

        return !result.isEmpty();
    }

    @Override
    public KafkaQueryEngine findByTopicAndBootstrapServers(String topic, String bootstrapServers) {
        return null;
    }
}

