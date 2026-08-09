package io.github.prakash.kqe.operator.kubernetes.controller;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.github.prakash.kqe.operator.api.utils.QueryEngineRegistry;
import io.github.prakash.kqe.operator.kubernetes.crd.KafkaQueryEngine;
import io.github.prakash.kqe.operator.kubernetes.crd.KafkaQueryEngineSpec;
import io.github.prakash.kqe.operator.kubernetes.crd.KafkaQueryEngineStatus;
import io.github.prakash.kqe.operator.kubernetes.crd.State;
import io.github.prakash.kqe.operator.kubernetes.model.KubernetesLabels;
import io.github.prakash.kqe.operator.kubernetes.service.ConfigMapService;
import io.github.prakash.kqe.operator.kubernetes.service.DeploymentService;
import io.github.prakash.kqe.operator.kubernetes.service.IndexStorageService;
import io.github.prakash.kqe.operator.kubernetes.service.K8svcService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaQueryEngineReconciler {

    private final KubernetesClient kubernetesClient;
    private final ConfigMapService configMapService;
    private final DeploymentService deploymentService;
    private final K8svcService k8svcService;
    private final  QueryEngineRegistry queryEngineRegistry;
    private final IndexStorageService indexStorageService;


    @Value("${k8s.namespace}")
    private String nameSpace;



    @Scheduled(fixedDelay = 30000) // Periodic reconciliation
    public void reconcileAll() {
        System.out.println("Starting reconcile ");
        // 1. Get ALL CRs from the cluster and ns
        List<KafkaQueryEngine> resources = kubernetesClient
                .resources(KafkaQueryEngine.class)
                .inNamespace(nameSpace)
                .list()
                .getItems();

        // 2. For each CR, ensure desired state
        for (KafkaQueryEngine resource : resources) {
            try {
                reconcileResource(resource);
            } catch (Exception e) {
                log.error("Failed to reconcile: {}",
                        resource.getMetadata().getName(), e);
            }
        }

        // 3. Garbage collection: Clean up orphaned resources
        // use owner ref for Garbage collection
    }

    public void reconcileResource(KafkaQueryEngine resource) {
        String name = resource.getMetadata().getName();
        String namespace = resource.getMetadata().getNamespace();
        KafkaQueryEngineSpec spec = resource.getSpec();

        log.info("Reconciling: {}/{}", namespace, name);

        try {


            KafkaQueryEngineStatus status = resource.getStatus();

            if (status != null && status.getState() == State.COMPLETED) {
                log.info("KQE {} is in  Completed state",resource.getMetadata().getName());
                cleanupResource(resource);
                return;
            }

            if (ttlExpired(resource)) {
                log.info("TTL Expired for KQE {}",resource.getMetadata().getName());
                cleanupResource(resource);
                return;
            }

            configMapService.reconcile(resource);
            deploymentService.reconcile(resource);
            k8svcService.reconcile(resource);

            boolean deploymentReady = deploymentService.isReady(resource);

            boolean serviceReady = k8svcService.isReady(resource);

            log.info("deployment ready : {} service ready :{} ",deploymentReady,serviceReady);

            if (deploymentReady && serviceReady) {
                updateStatus(resource, State.RUNNING, "Query Engine is ready");
                queryEngineRegistry.register(resource);
            } else {
                updateStatus(resource,
                        State.PENDING,
                        "Waiting for deployment/service");
            }

        } catch (Exception e) {
            log.error("Failed to reconcile: {}/{}", namespace, name, e);
            updateStatus(resource, State.FAILED, "Error: " + e.getMessage());
        }
    }

    private void updateStatus(
            KafkaQueryEngine resource,
            State state,
            String message) {

        KafkaQueryEngine latest = kubernetesClient.resources(KafkaQueryEngine.class)
                .inNamespace(resource.getMetadata().getNamespace())
                .withName(resource.getMetadata().getName())
                .get();

        if(latest==null){
            log.info( "Query Engine CR {}/{} no longer exists",
                    resource.getMetadata().getNamespace(),
                    resource.getMetadata().getName());
            return;
        }

        KafkaQueryEngineStatus status = latest.getStatus();

        if (status == null) {
            status = new KafkaQueryEngineStatus();
            String serviceName = resource.getMetadata().getName() + "-service";
            status.setServiceEndpoint("http://" + serviceName + ":8080");
            String indexPath =
                    "/index/" + resource.getSpec().getEngineId();
            status.setIndexPath(indexPath);
        }
        status.setState(state);
        status.setMessage(message);
        status.setObservedGeneration(
                latest.getMetadata().getGeneration());

        // Set only once
        if (status.getStartedAt() == null && state == State.RUNNING) {
            status.setStartedAt(OffsetDateTime.now());
            log.info(
                    "KQE {} started at {}",
                    latest.getMetadata().getName(),
                    status.getStartedAt());
        }

        latest.setStatus(status);

        kubernetesClient.resources(KafkaQueryEngine.class)
                .inNamespace(resource.getMetadata().getNamespace())
                .resource(latest)
                .updateStatus();
    }

    public void cleanupResource(KafkaQueryEngine resource) {
        log.info("Clearing all resources created for  KQE {}",resource.getMetadata().getName());
        queryEngineRegistry.unregister(resource);

        configMapService.delete(resource);
        deploymentService.delete(resource);
        k8svcService.delete(resource);
        // wait for kqe pod to get deleted and remove index
        waitUntilPodDeleted(resource);
        indexStorageService.delete(resource);

        updateStatus(resource, State.COMPLETED, "TTL Expired");

    }


    private boolean ttlExpired(KafkaQueryEngine resource) {
        log.info("Checking TTL for KQE {}",resource.getMetadata().getName());

        KafkaQueryEngineStatus status = resource.getStatus();
        if (status == null || status.getStartedAt() == null) {
            return false;
        }

        return OffsetDateTime.now().isAfter(
                status.getStartedAt()
                        .plusSeconds(resource.getSpec().getTtl()));
    }


    public void waitUntilPodDeleted(KafkaQueryEngine resource) {

        String namespace = resource.getMetadata().getNamespace();
        String engineId = resource.getSpec().getEngineId();

        log.info("Waiting for KQE pod {} to terminate", engineId);

        // Wait up to 5 minutes
        int maxRetries = 30;
        int retries = 0;

        while (retries < maxRetries) {
            List<Pod> pods = kubernetesClient.pods()
                    .inNamespace(namespace)
                    .withLabels(KubernetesLabels.forEngine(engineId))
                    .list()
                    .getItems();

            if (pods.isEmpty()) {
                log.info("KQE pod {} terminated", engineId);
                return;
            }

            try {
                Thread.sleep(10000); // Wait 10 seconds
                retries++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        log.warn("Timed out waiting for KQE pod {} to terminate", engineId);
    }
}
