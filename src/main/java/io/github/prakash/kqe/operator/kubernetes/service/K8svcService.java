package io.github.prakash.kqe.operator.kubernetes.service;
import io.github.prakash.kqe.operator.kubernetes.crd.KafkaQueryEngine;

public interface K8svcService {
    void reconcile(KafkaQueryEngine resource);

    void delete(KafkaQueryEngine resource);

    public boolean isReady(KafkaQueryEngine resource);

    }
