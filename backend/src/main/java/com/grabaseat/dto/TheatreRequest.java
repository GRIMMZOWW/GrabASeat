package com.grabaseat.dto;

import jakarta.validation.constraints.NotBlank;

public class TheatreRequest {

    @NotBlank(message = "Theatre name is required")
    private String name;

    @NotBlank(message = "City is required")
    private String city;

    public TheatreRequest() {
    }

    public TheatreRequest(String name, String city) {
        this.name = name;
        this.city = city;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
