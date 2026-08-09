package io.github.prakash.kqe.operator.kubernetes.controller;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.github.prakash.kqe.operator.kubernetes.crd.KafkaQueryEngine;
import io.github.prakash.kqe.operator.kubernetes.crd.KafkaQueryEngineSpec;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaQueryEngineWatcher {

    private final KubernetesClient client;
    private final KafkaQueryEngineReconciler reconciler;

    @PostConstruct
    public void watch() {

        client.resources(KafkaQueryEngine.class)
                .inform(new ResourceEventHandler<>() {

                    @Override
                    public void onAdd(KafkaQueryEngine resource) {

                        log.info("CREATE {}", resource.getMetadata().getName());

                        reconciler.reconcileResource(resource);
                    }

                    @Override
                    public void onUpdate(
                            KafkaQueryEngine oldResource,
                            KafkaQueryEngine newResource) {

                        if (specChanged(oldResource, newResource)) {
                            log.info("UPDATE {}", newResource.getMetadata().getName());
                            reconciler.reconcileResource(newResource);
                        }

                    }

                    @Override
                    public void onDelete(
                            KafkaQueryEngine resource,
                            boolean deletedFinalStateUnknown) {

                        log.info("DELETE {}", resource.getMetadata().getName());
                        reconciler.cleanupResource(resource);
                    }
                });

        log.info("KafkaQueryEngine watcher started");
    }


    /**
     * Check if the spec has changed between old and new resource
     */
    private boolean specChanged(KafkaQueryEngine old, KafkaQueryEngine newResource) {
        if (old == null || newResource == null) {
            return true;
        }

        KafkaQueryEngineSpec oldSpec = old.getSpec();
        KafkaQueryEngineSpec newSpec = newResource.getSpec();

        // Compare all fields that matter
        return !Objects.equals(oldSpec.getTopic(), newSpec.getTopic())
                || !Objects.equals(oldSpec.getBootstrapServers(), newSpec.getBootstrapServers())
                || !Objects.equals(oldSpec.getTtl(), newSpec.getTtl())
                || !Objects.equals(oldSpec.getKafkaConfig(), newSpec.getKafkaConfig());
    }
}
