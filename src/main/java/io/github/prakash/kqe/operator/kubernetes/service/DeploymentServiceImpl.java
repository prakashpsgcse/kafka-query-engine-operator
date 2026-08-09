package io.github.prakash.kqe.operator.kubernetes.service;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentStatus;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.github.prakash.kqe.operator.kubernetes.crd.KafkaQueryEngine;
import io.github.prakash.kqe.operator.kubernetes.crd.KafkaQueryEngineSpec;
import io.github.prakash.kqe.operator.kubernetes.model.KubernetesLabels;
import io.github.prakash.kqe.operator.kubernetes.model.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
@Slf4j
@Service
@RequiredArgsConstructor
public class DeploymentServiceImpl implements DeploymentService {

    private static final String APP_NAME = "kafka-query-engine";
    private static final String CONFIG_VOLUME = "config";

    /**
     * Replace with your actual image.
     */
    private static final String IMAGE =
            "kqe:latest";

    private final KubernetesClient kubernetesClient;


    @Override
    public void reconcile(KafkaQueryEngine resource) {

        String name = resource.getMetadata().getName();
        String namespace = resource.getMetadata().getNamespace();
        KafkaQueryEngineSpec spec = resource.getSpec();

        Deployment desiredDeployment = build(resource);
        Deployment currentDeployment = kubernetesClient.apps().deployments()
                .inNamespace(namespace)
                .withName(name + "-deployment")
                .get();

        if (currentDeployment == null) {
            log.info("Deployment dosen't exists. Creating Deployment: {}", name + "-deployment");
            kubernetesClient.apps().deployments()
                    .inNamespace(namespace)
                    .resource(desiredDeployment)
                    .create();
            log.info("Created Deployment: {}", name + "-deployment");
        } else if (!deploymentEquals(currentDeployment, desiredDeployment)) {
            log.info("Deployment changed. updating Deployment: {}", name + "-deployment");
            kubernetesClient.apps().deployments()
                    .inNamespace(namespace)
                    .resource(desiredDeployment)
                    .update();
            log.info("Updated Deployment: {}", name + "-deployment");
        }
    }

    private boolean deploymentEquals(Deployment current, Deployment desired) {
        // Compare replicas, image, env, volumes, etc.
        return current.getSpec().getReplicas().equals(desired.getSpec().getReplicas())
                && current.getSpec().getTemplate().getSpec().getContainers()
                .get(0).getImage()
                .equals(desired.getSpec().getTemplate().getSpec().getContainers()
                        .get(0).getImage());
    }

    @Override
    public void delete(KafkaQueryEngine resource) {

         kubernetesClient.apps()
                .deployments()
                .inNamespace(resource.getMetadata().getNamespace())
                .withName(resource.getMetadata().getName()+"-deployment")
                .delete();
    }

    @Override
    public boolean isReady(KafkaQueryEngine resource) {
        log.info("checking deployment status for {}",resource.getMetadata().getName());
        Deployment deployment = kubernetesClient.apps()
                .deployments()
                .inNamespace(resource.getMetadata().getNamespace())
                .withName(resource.getMetadata().getName() + "-deployment")
                .get();

        if (deployment == null || deployment.getStatus() == null) {
            return false;
        }

        DeploymentStatus status = deployment.getStatus();

        return status.getReadyReplicas() != null
                && status.getReadyReplicas() > 0
                && status.getReadyReplicas().equals(status.getReplicas());
    }


    private Deployment build(KafkaQueryEngine resource) {
        return new DeploymentBuilder()
                .withNewMetadata()
                .withName(deploymentName(resource)+"-deployment")
                .withNamespace(namespace(resource))
                .withLabels(KubernetesLabels.forEngine(resource.getSpec().getEngineId()))
                .withOwnerReferences(Utils.ownerReference(resource))
                .endMetadata()

                .withNewSpec()
                .withReplicas(1)
                .withNewSelector()
                .addToMatchLabels(KubernetesLabels.forEngine(resource.getSpec().getEngineId()))
                .endSelector()

                .withNewTemplate()
                .withNewMetadata()
                .withLabels(KubernetesLabels.forEngine(resource.getSpec().getEngineId()))
                .endMetadata()

                .withNewSpec()
                .addToVolumes(
                        new VolumeBuilder()
                                .withName(CONFIG_VOLUME)
                                .withNewConfigMap()
                                .withName(configMapName(resource))
                                .endConfigMap()
                                .build(),
                        new VolumeBuilder()
                                .withName("kqe-index")
                                .withNewPersistentVolumeClaim()
                                .withClaimName("kqe-index-pvc")
                                .endPersistentVolumeClaim()
                                .build()
                )


                .addToContainers(
                        new ContainerBuilder()
                                .withName(APP_NAME)
                                .withImage(IMAGE)
                                .withImagePullPolicy("IfNotPresent")

                                .addNewPort()
                                .withContainerPort(8080)
                                .endPort()

                                .addNewEnv()
                                .withName("SPRING_CONFIG_ADDITIONAL_LOCATION")
                                .withValue("file:/config/")
                                .endEnv()

                                .addToVolumeMounts(
                                        new VolumeMountBuilder()
                                                .withName(CONFIG_VOLUME)
                                                .withMountPath("/config")
                                                .build(),
                                        // Shared NFS PVC
                                        new VolumeMountBuilder()
                                                .withName("kqe-index")
                                                .withMountPath("/index")
                                                .build()
                                )

                                .build()
                )

                .endSpec()

                .endTemplate()

                .endSpec()

                .build();
    }


    private String deploymentName(KafkaQueryEngine resource) {
        return resource.getMetadata().getName();
    }

    private String configMapName(KafkaQueryEngine resource) {
        return resource.getMetadata().getName() + "-config";
    }

    private String namespace(KafkaQueryEngine resource) {
        return resource.getMetadata().getNamespace();
    }
}
