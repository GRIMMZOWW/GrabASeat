package com.grabaseat.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ShowResponse {

    private Long id;
    private Long movieId;
    private String movieTitle;
    private Long theatreId;
    private String theatreName;
    private String city;
    private Integer screenNumber;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal price;

    public ShowResponse() {
    }

    public ShowResponse(Long id, Long movieId, String movieTitle, Long theatreId, String theatreName, String city, Integer screenNumber, LocalDateTime startTime, LocalDateTime endTime, BigDecimal price) {
        this.id = id;
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.theatreId = theatreId;
        this.theatreName = theatreName;
        this.city = city;
        this.screenNumber = screenNumber;
        this.startTime = startTime;
        this.endTime = endTime;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public Long getMovieId() {
        return movieId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public Long getTheatreId() {
        return theatreId;
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

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
