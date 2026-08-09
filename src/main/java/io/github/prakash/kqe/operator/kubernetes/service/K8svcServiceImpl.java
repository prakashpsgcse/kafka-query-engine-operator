package io.github.prakash.kqe.operator.kubernetes.service;
import io.fabric8.kubernetes.api.model.Endpoints;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.github.prakash.kqe.operator.kubernetes.crd.KafkaQueryEngine;
import io.github.prakash.kqe.operator.kubernetes.crd.KafkaQueryEngineSpec;
import io.github.prakash.kqe.operator.kubernetes.model.KubernetesLabels;
import io.github.prakash.kqe.operator.kubernetes.model.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@Slf4j
@org.springframework.stereotype.Service
public class K8svcServiceImpl implements K8svcService {

    private static final String APP_NAME = "kafka-query-engine";

    @Autowired
    private  KubernetesClient kubernetesClient;

    @Override
    public void reconcile(KafkaQueryEngine resource) {

        String name = resource.getMetadata().getName();
        String namespace = resource.getMetadata().getNamespace();
        KafkaQueryEngineSpec spec = resource.getSpec();

        Service desiredService = buildService(resource);
        Service currentService = kubernetesClient.services()
                .inNamespace(namespace)
                .withName(name + "-service")
                .get();

        if (currentService == null) {
            log.info("Service dosen't exists. Creating Service: {}", name + "-service");
            kubernetesClient.services()
                    .inNamespace(namespace)
                    .resource(desiredService)
                    .create();
            log.info("Created Service: {}", name + "-service");
        } else if (!serviceEquals(currentService, desiredService)) {
            log.info("Serivce changed. Updated Service: {}", name + "-service");
            kubernetesClient.services()
                    .inNamespace(namespace)
                    .resource(desiredService)
                    .update();
            log.info("Updated Service: {}", name + "-service");
        }
    }

    @Override
    public void delete(KafkaQueryEngine resource) {
        String name = resource.getMetadata().getName();
        String namespace = resource.getMetadata().getNamespace();
        kubernetesClient.services()
                .inNamespace(namespace)
                .withName(name + "-service")
                .delete();
    }



    private boolean serviceEquals(Service current, Service desired) {
        return current.getSpec().getPorts().equals(desired.getSpec().getPorts())
                && current.getSpec().getSelector().equals(desired.getSpec().getSelector());
    }

    /**
     * Build Service from CR spec
     */
    private Service buildService(KafkaQueryEngine resource) {
        String name = resource.getMetadata().getName();

        return new ServiceBuilder()
                .withNewMetadata()
                .withName(name+ "-service")  // Changed from name + "-service" to just name
                .withNamespace(resource.getMetadata().getNamespace())
                .withLabels(KubernetesLabels.forEngine(resource.getSpec().getEngineId()))
                .withOwnerReferences(Utils.ownerReference(resource))
                .endMetadata()
                .withNewSpec()
                .withSelector(KubernetesLabels.forEngine(resource.getSpec().getEngineId())) // or use getSelectorLabels(resource) if it returns the correct labels
                .addNewPort()
                .withName("http")
                .withPort(8080)
                .withTargetPort(new IntOrString(8080))
                .endPort()
                .withType("ClusterIP")
                .endSpec()
                .build();
    }

    public boolean isReady(KafkaQueryEngine resource) {

        Endpoints endpoints = kubernetesClient.endpoints()
                .inNamespace(resource.getMetadata().getNamespace())
                .withName(resource.getMetadata().getName()+"-service")
                .get();

        if (endpoints == null || endpoints.getSubsets() == null) {
            return false;
        }

        return endpoints.getSubsets().stream()
                .anyMatch(subset ->
                        subset.getAddresses() != null &&
                                !subset.getAddresses().isEmpty());
    }

}
