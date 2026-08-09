package io.github.prakash.kqe.operator.kubernetes.model;

import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.api.model.OwnerReferenceBuilder;
import io.github.prakash.kqe.operator.kubernetes.crd.KafkaQueryEngine;


public class Utils {
    public static OwnerReference ownerReference(KafkaQueryEngine resource) {
        return new OwnerReferenceBuilder()
                .withApiVersion(resource.getApiVersion())
                .withKind(resource.getKind())
                .withName(resource.getMetadata().getName())
                .withUid(resource.getMetadata().getUid())
                .withController(true)
                .withBlockOwnerDeletion(true)
                .build();
    }
}
