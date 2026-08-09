package io.github.prakash.kqe.operator.api.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.util.Map;

@Data
@Getter
@Setter
public class QueryEngineRequest {
    @NotBlank(message = "Topic is required")
    private String topic;
    @NotBlank(message = "Bootstrap servers are required")
    private String bootstrapServers;
    @NotNull(message = "TTL is required")
    @Min(value = 100, message = "TTL must be at least 100 seconds")
    private Integer ttl;
    private PayloadType keyPayloadType;
    @NotNull(message = "Value payload type is required")

    private PayloadType valuePayloadType;
    private String schemaRegistryUrl;
    private Map<String, String> kafkaConfig;
}
