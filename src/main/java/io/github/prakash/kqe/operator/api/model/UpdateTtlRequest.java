package io.github.prakash.kqe.operator.api.model;

import javax.validation.constraints.Min;

public record UpdateTtlRequest(@Min(100) Integer ttl) {
}
