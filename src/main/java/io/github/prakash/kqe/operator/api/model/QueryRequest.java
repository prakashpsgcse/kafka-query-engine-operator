package io.github.prakash.kqe.operator.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QueryRequest {

    @NotBlank
    private String topic;

    @NotBlank
    private String bootstrapServers;

    @NotNull QueryField field;

    @NotBlank
    private String query;

}
