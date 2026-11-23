package com.grabaseat.dto;

import java.time.LocalDateTime;

public class MovieShowResponse {

    private Long id;
    private String theatreName;
    private String city;
    private Integer screenNumber;
    private LocalDateTime startTime;
    private Double price;

    public MovieShowResponse() {
    }

    public MovieShowResponse(Long id, String theatreName, String city, Integer screenNumber, LocalDateTime startTime, Double price) {
        this.id = id;
        this.theatreName = theatreName;
        this.city = city;
        this.screenNumber = screenNumber;
        this.startTime = startTime;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public String getTheatreName() {
        return theatreName;
    }

    public String getCity() {
        return city;
    }

    public Integer getScreenNumber() {
        return screenNumber;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public Double getPrice() {
        return price;
    }
}
