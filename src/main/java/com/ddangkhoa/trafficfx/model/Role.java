package com.ddangkhoa.trafficfx.model;

public enum Role {
    ADMIN("Quản trị viên"),
    TECHNICIAN("Kỹ thuật viên"),
    ANALYST("Phân tích viên");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
