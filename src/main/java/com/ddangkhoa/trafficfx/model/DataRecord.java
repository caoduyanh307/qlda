package com.ddangkhoa.trafficfx.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class DataRecord {
    private final LinkedHashMap<String, String> values = new LinkedHashMap<>();

    public DataRecord() {}

    public DataRecord(Map<String, String> initialValues) {
        values.putAll(initialValues);
    }

    public String get(String columnName) {
        return values.getOrDefault(columnName, "");
    }

    public void put(String columnName, String value) {
        values.put(columnName, value == null ? "" : value);
    }

    public LinkedHashMap<String, String> getValues() {
        return values;
    }
}
