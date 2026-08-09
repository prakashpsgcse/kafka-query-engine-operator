package io.github.prakash.kqe.operator.api.model;

import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryEngineResponse {
    private String engineId;
    private String topic;
    private String bootstrapServers;
    private ZonedDateTime createdAt;
}
