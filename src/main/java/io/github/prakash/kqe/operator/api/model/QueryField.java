package io.github.prakash.kqe.operator.api.model;

public enum QueryField {

    KEY("key"),
    VALUE("value");

    private final String fieldName;

    QueryField(String fieldName) {
        this.fieldName = fieldName;
    }

    public String fieldName() {
        return fieldName;
    }
}