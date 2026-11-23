package com.grabaseat.dto;

public class TheatreResponse {

    private Long id;
    private String name;
    private String city;

    public TheatreResponse() {
    }

    public TheatreResponse(Long id, String name, String city) {
        this.id = id;
        this.name = name;
        this.city = city;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }
}
