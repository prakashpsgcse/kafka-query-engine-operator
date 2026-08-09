package io.github.prakash.kqe.operator.api.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MessageRequest {
    @NotBlank String topic;
    @NotBlank String bootstrapServers;
   int partition;
   long offset;
}
