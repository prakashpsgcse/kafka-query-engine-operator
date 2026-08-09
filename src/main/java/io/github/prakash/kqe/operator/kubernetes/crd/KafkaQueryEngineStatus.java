package io.github.prakash.kqe.operator.kubernetes.crd;


import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
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

    @JsonPropertyDescription("Time when the Query Engine became RUNNING. Used for TTL calculation.")
    private OffsetDateTime startedAt;

    private Long observedGeneration;

    @JsonPropertyDescription("Index path in shared PV")
    private String indexPath;
}
