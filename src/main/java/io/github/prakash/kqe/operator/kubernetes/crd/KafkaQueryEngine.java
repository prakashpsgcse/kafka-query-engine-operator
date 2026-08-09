package io.github.prakash.kqe.operator.kubernetes.crd;


import io.fabric8.crd.generator.annotation.AdditionalPrinterColumn;
import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.*;

@Group("io.github.prakash")
@Version("v1alpha1")
@Singular("kafkaqueryengine")
@Plural("kafkaqueryengines")
@ShortNames("kqe")
@AdditionalPrinterColumn(name = "Topic", jsonPath = ".spec.topic", type = AdditionalPrinterColumn.Type.STRING)
@AdditionalPrinterColumn(name = "Brokers", jsonPath = ".spec.bootstrapServers", type = AdditionalPrinterColumn.Type.STRING)
@AdditionalPrinterColumn(name = "State", jsonPath = ".status.state", type = AdditionalPrinterColumn.Type.STRING)
@AdditionalPrinterColumn(name = "Age", jsonPath = ".metadata.creationTimestamp", type = AdditionalPrinterColumn.Type.DATE)
public class KafkaQueryEngine extends CustomResource<KafkaQueryEngineSpec, KafkaQueryEngineStatus>
        implements Namespaced {
}
