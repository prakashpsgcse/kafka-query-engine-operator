package io.github.prakash.kqe.operator.kubernetes.model;

import java.util.HashMap;
import java.util.Map;

public final class KubernetesLabels {
    public static final String NAME = "app.kubernetes.io/name";
    public static final String INSTANCE = "app.kubernetes.io/instance";
    public static final String MANAGED_BY = "app.kubernetes.io/managed-by";


    private KubernetesLabels() {}

    public static Map<String, String> forEngine(String engineId) {
        Map<String, String> labels = new HashMap<>();
        labels.put("app.kubernetes.io/name", "kafka-query-engine");
        labels.put("app.kubernetes.io/instance", engineId);
        labels.put("app.kubernetes.io/managed-by", "kafka-query-engine-operator");
        return labels;
    }

}
