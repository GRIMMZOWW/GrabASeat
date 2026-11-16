package com.grabaseat.dto;

import com.grabaseat.model.Role;

public class LoginResponse {

    private String token;
    private String name;
    private Role role;

    public LoginResponse(String token, String name, Role role) {
        this.token = token;
        this.name = name;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public String getName() {
        return name;
    }

    public Role getRole() {
        return role;
    }
}

