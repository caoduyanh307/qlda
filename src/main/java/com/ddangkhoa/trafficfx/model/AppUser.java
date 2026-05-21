package com.ddangkhoa.trafficfx.model;

public class AppUser {
    private final String username;
    private final String fullName;
    private final Role role;

    public AppUser(String username, String fullName, Role role) {
        this.username = username;
        this.fullName = fullName;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public Role getRole() {
        return role;
    }
}
