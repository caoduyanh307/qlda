package com.ddangkhoa.trafficfx.model;

public class DemoRecord {
    private final String id;
    private final String name;
    private final String status;
    private final String note;

    public DemoRecord(String id, String name, String status, String note) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.note = note;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public String getNote() {
        return note;
    }
}
