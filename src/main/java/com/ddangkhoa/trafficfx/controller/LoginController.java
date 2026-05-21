package com.ddangkhoa.trafficfx.controller;

import java.util.Optional;

import com.ddangkhoa.trafficfx.model.AppUser;
import com.ddangkhoa.trafficfx.service.AuthService;

public class LoginController {
    private final AuthService authService;

    public LoginController() {
        this.authService = new AuthService();
    }

    public Optional<AppUser> authenticate(String username, String password) {
        return authService.login(username, password);
    }
}
