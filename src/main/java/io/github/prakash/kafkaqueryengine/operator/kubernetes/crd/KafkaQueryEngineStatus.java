package io.github.prakash.kafkaqueryengine.operator.kubernetes.crd;


import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.fabric8.crd.generator.annotation.PrinterColumn;
import io.fabric8.generator.annotation.Required;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonClassDescription("Status of the Kafka Query Engine")
public class KafkaQueryEngineStatus{

    @Required
    @JsonPropertyDescription("Current state of the engine: Running, Failed, Pending, or Stopped")
    private State state;

    @JsonPropertyDescription("Human-readable message describing the current state")
    private String message;

    @JsonPropertyDescription("Service endpoint for accessing the search API")
    private String serviceEndpoint;

    private OffsetDateTime lastReconciledAt;

    private Long observedGeneration;
}
