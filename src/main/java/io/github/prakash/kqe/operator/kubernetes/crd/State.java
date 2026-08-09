package io.github.prakash.kqe.operator.kubernetes.crd;

public enum State {

        PENDING("The KafkaQueryEngine resource has been created and is waiting to be reconciled."),

        CREATING("The Operator is creating the ConfigMap, Deployment, Service, and other required resources."),

        RUNNING("The Kafka Query Engine is running and processing data."),

        COMPLETED("The configured TTL has expired. All managed resources have been cleaned up successfully."),

        FAILED("The Operator encountered an unrecoverable error while provisioning or managing the engine.");

        private final String description;

        State(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

}
