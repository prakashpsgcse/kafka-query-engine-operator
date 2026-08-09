package io.github.prakash.kqe.operator.kubernetes.service;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.github.prakash.kqe.operator.kubernetes.crd.KafkaQueryEngine;
import io.github.prakash.kqe.operator.kubernetes.crd.KafkaQueryEngineSpec;
import io.github.prakash.kqe.operator.kubernetes.model.KubernetesLabels;
import io.github.prakash.kqe.operator.kubernetes.model.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class ConfigMapServiceImpl implements ConfigMapService {
    private static final String CONFIG_FILE = "application.properties";
    @Autowired
    private  KubernetesClient kubernetesClient;

    @Value("${k8s.namespace}")
    private String nameSpace;


    @Override
    public void reconcile(KafkaQueryEngine resource) {
        // ===== 1. CREATE OR UPDATE CONFIGMAP =====
        String name = resource.getMetadata().getName();
        String namespace = resource.getMetadata().getNamespace();
        KafkaQueryEngineSpec spec = resource.getSpec();

        ConfigMap desiredCM = buildConfigMap(resource);
        ConfigMap currentCM = kubernetesClient.configMaps()
                .inNamespace(namespace)
                .withName(name + "-config")
                .get();

        if (currentCM == null) {
            // Create if doesn't exist
            kubernetesClient.configMaps()
                    .inNamespace(namespace)
                    .resource(desiredCM)
                    .create();
            log.info("Created ConfigMap: {}", desiredCM.toString());
        } else if (!configMapEquals(currentCM, desiredCM)) {
            // Update if changed
            kubernetesClient.configMaps()
                    .inNamespace(namespace)
                    .resource(desiredCM)
                    .update();
            log.info("Updated ConfigMap: {}", desiredCM.toString());
        }


    }

    @Override
    public void delete(KafkaQueryEngine resource) {
        String name = resource.getMetadata().getName();
        String namespace = resource.getMetadata().getNamespace();
        kubernetesClient.configMaps()
                .inNamespace(namespace)
                .withName(name + "-config")
                .delete();

    }

    /**
     * Build ConfigMap from CR spec
     */
    private ConfigMap buildConfigMap(KafkaQueryEngine resource) {

        String name = resource.getMetadata().getName();
        KafkaQueryEngineSpec spec = resource.getSpec();

        // Build ConfigMap with application.properties
        Map<String, String> data = new HashMap<>();
        StringBuilder props = new StringBuilder();

        //mandatory props kafka.topic,value.payload.type,spring.kafka.bootstrap-servers
        props.append("kafka.topic=").append(spec.getTopic()).append("\n");
        props.append("spring.kafka.bootstrap-servers=").append(spec.getBootstrapServers()).append("\n");
        props.append("key.payload.type=").append(spec.getKeyPayloadType() != null ? spec.getKeyPayloadType() : "STRING").append("\n");
        props.append("value.payload.type=").append(spec.getValuePayloadType() != null ? spec.getValuePayloadType() : "STRING").append("\n");
        props.append("schema.registry.url=").append(spec.getSchemaRegistryUrl() != null ? spec.getSchemaRegistryUrl() : "http://localhost:8081").append("\n");

        //generate based on p
        String indexPath = "/index/" + resource.getSpec().getEngineId();
        props.append("index.path=")
                .append(indexPath)
                .append("\n");

        // Add custom Kafka config if present
        // all props must be speing.kafka.properties.{apache kafka client props}
        if (spec.getKafkaConfig() != null) {
            for (Map.Entry<String, String> entry : spec.getKafkaConfig().entrySet()) {
                props.append("kafka.consumer.properties.")
                        .append(entry.getKey())
                        .append("=")
                        .append(entry.getValue())
                        .append("\n");
            }
        }

        data.put("application.properties", props.toString());


        log.info("Configmap generated for KQE {}",data);



        return new ConfigMapBuilder()
                .withNewMetadata()
                .withName(name + "-config")
                .withNamespace(resource.getMetadata().getNamespace())
                .withLabels(KubernetesLabels.forEngine(resource.getSpec().getEngineId()))
                .withOwnerReferences(Utils.ownerReference(resource))
                .endMetadata()
                .withData(data)
                .build();
    }

    private boolean configMapEquals(ConfigMap current, ConfigMap desired) {
        // Compare data, labels, annotations, etc.
        return current.getData().equals(desired.getData())
                && current.getMetadata().getLabels().equals(desired.getMetadata().getLabels());
    }
}
