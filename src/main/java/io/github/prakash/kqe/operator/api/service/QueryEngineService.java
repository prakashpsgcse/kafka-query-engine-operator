package io.github.prakash.kqe.operator.api.service;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.StatusDetails;
import io.github.prakash.kqe.operator.api.exception.QueryEngineNotFoundException;
import io.github.prakash.kqe.operator.api.model.QueryEngineRequest;
import io.github.prakash.kqe.operator.api.model.QueryEngineResponse;
import io.github.prakash.kqe.operator.api.utils.EngineIdGenerator;
import io.github.prakash.kqe.operator.kubernetes.crd.KafkaQueryEngine;
import io.github.prakash.kqe.operator.kubernetes.crd.KafkaQueryEngineSpec;
import io.github.prakash.kqe.operator.kubernetes.model.KubernetesLabels;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class QueryEngineService {

    @Value("${k8s.namespace}")
    private String nameSpace;

    @Autowired
    private KubernetesService kubernetesService;
    public QueryEngineResponse create(QueryEngineRequest request) {
        KafkaQueryEngine kqe = createKafkaQueryEngineCR(request);
        KafkaQueryEngine createdKqe = kubernetesService.create(kqe);
        return toResponse(createdKqe);
    }

    public QueryEngineResponse get(String name) {
        KafkaQueryEngine resource = kubernetesService
                .get(nameSpace, name)
                .orElseThrow(() -> new QueryEngineNotFoundException(name));

        return toResponse(resource);
    }

    public List<QueryEngineResponse> list() {
        return kubernetesService.list(nameSpace)
                .stream()
                .map(this::toResponse)
                .toList();    }

    public QueryEngineResponse updateTtl(String name, Integer ttl) {

        KafkaQueryEngine resource = kubernetesService
                .get(nameSpace, name)
                .orElseThrow(() -> new QueryEngineNotFoundException(name));

        // update TTL , get TTL and add new TTL
        resource.getSpec().setTtl(
                resource.getSpec().getTtl() + ttl
        );

        KafkaQueryEngine updated = kubernetesService.update(resource);

        return toResponse(updated);
    }

    public void delete(String name) {
        boolean deleted = kubernetesService.delete(nameSpace, name);

        if (!deleted) {
            throw new QueryEngineNotFoundException(name);
        }
    }


    private KafkaQueryEngine createKafkaQueryEngineCR(QueryEngineRequest request) {

        String engineId= EngineIdGenerator.generate(request.getBootstrapServers(),request.getTopic());

        log.info("Creating KQE for request {}",request);
        KafkaQueryEngine kqe = new KafkaQueryEngine();
        ObjectMeta metadata = new ObjectMeta();
        metadata.setName(engineId);
        metadata.setNamespace(nameSpace);
        metadata.setLabels(KubernetesLabels.forEngine(engineId));
        kqe.setMetadata(metadata);

        KafkaQueryEngineSpec spec = new KafkaQueryEngineSpec();
        spec.setEngineId(engineId);
        spec.setTopic(request.getTopic());
        spec.setBootstrapServers(request.getBootstrapServers());
        spec.setTtl(request.getTtl());
        spec.setKafkaConfig(request.getKafkaConfig());
        kqe.setSpec(spec);
        log.info("Created KQE {}",kqe);
        return kqe;
    }
    private QueryEngineResponse toResponse(KafkaQueryEngine resource) {

        QueryEngineResponse response = new QueryEngineResponse();
        response.setEngineId(resource.getSpec().getEngineId());
        response.setTopic(resource.getSpec().getTopic());
        response.setBootstrapServers(resource.getSpec().getBootstrapServers());
        return response;
    }

}
