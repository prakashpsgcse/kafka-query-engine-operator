package io.github.prakash.kqe.operator.api.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IndexRequest{
    @NotBlank String topic;
    @NotBlank String bootstrapServers;
}
