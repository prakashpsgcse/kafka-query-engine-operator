package io.github.prakash.kqe.operator.kubernetes.crd;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.fabric8.generator.annotation.Min;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Plural;
import io.fabric8.kubernetes.model.annotation.ShortNames;
import io.fabric8.kubernetes.model.annotation.Version;
import io.fabric8.crd.generator.annotation.PrinterColumn;
import io.fabric8.generator.annotation.Required;
import lombok.*;

import java.util.Map;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KafkaQueryEngineSpec {

    @Required
    @JsonPropertyDescription("Unique identifier for the Kafka Query Engine instance. Used to identify and manage all Kubernetes resources associated with this engine.")
    private String engineId;

    @Required
    @JsonPropertyDescription("Kafka topic to consume messages from")
    private String topic;
    @Required
    @JsonPropertyDescription("Kafka bootstrap server addresses")
    private String bootstrapServers;

    @Required
    @JsonPropertyDescription("Payload type for the value (e.g., STRING, AVRO, JSON, PROTOBUF). Defaults to STRING.")
    private String valuePayloadType = "STRING";  // Default to STRING

    @JsonPropertyDescription("Payload type for the key (e.g., STRING, AVRO, JSON, PROTOBUF). If not provided, key will be treated as null or ignored.")
    private String keyPayloadType;

    @JsonPropertyDescription("Schema Registry URL. Required when using AVRO or PROTOBUF payload types.")
    private String schemaRegistryUrl;

    @Required
    @JsonPropertyDescription("Time-to-live for indexed documents in seconds. Documents older than this will be removed.")
    @Min(100)
    private Integer ttl;

    @JsonPropertyDescription("Custom Kafka consumer configuration properties (e.g., group.id, auto.offset.reset)")
    private Map<String, String> kafkaConfig;
}
