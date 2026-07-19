package io.github.prakash.kafkaqueryengine.operator.kubernetes.crd;

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
    @JsonPropertyDescription("Kafka topic to consume messages from")
    private String topic;
    @Required
    @JsonPropertyDescription("Kafka bootstrap server addresses")
    private String bootstrapServers;
    @Required
    @JsonPropertyDescription("Time-to-live for indexed documents in seconds. Documents older than this will be removed.")
    @Min(100)
    private Integer ttl;

    @JsonPropertyDescription("Custom Kafka consumer configuration properties (e.g., group.id, auto.offset.reset)")
    private Map<String, String> kafkaConfig;
}
