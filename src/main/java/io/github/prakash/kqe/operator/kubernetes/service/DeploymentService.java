package io.github.prakash.kqe.operator.kubernetes.service;

import io.github.prakash.kqe.operator.kubernetes.crd.KafkaQueryEngine;

public interface DeploymentService {

    void reconcile(KafkaQueryEngine resource);

    void delete(KafkaQueryEngine resource);

    boolean isReady(KafkaQueryEngine resource);
}
