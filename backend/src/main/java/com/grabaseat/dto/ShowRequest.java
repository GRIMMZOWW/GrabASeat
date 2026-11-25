package com.grabaseat.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ShowRequest {

    @NotNull(message = "Movie ID is required")
    private Long movieId;

    @NotNull(message = "Theatre ID is required")
    private Long theatreId;

    @NotNull(message = "Screen number is required")
    @Min(value = 1, message = "Screen number must be at least 1")
    private Integer screenNumber;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be more than 0")
    private BigDecimal price;

    public ShowRequest() {
    }

    public ShowRequest(Long movieId, Long theatreId, Integer screenNumber, LocalDateTime startTime, BigDecimal price) {
        this.movieId = movieId;
        this.theatreId = theatreId;
        this.screenNumber = screenNumber;
        this.startTime = startTime;
        this.price = price;
    }

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public Long getTheatreId() {
        return theatreId;
    }

    public void setTheatreId(Long theatreId) {
        this.theatreId = theatreId;
    }

    public Integer getScreenNumber() {
        return screenNumber;
    }

    public void setScreenNumber(Integer screenNumber) {
        this.screenNumber = screenNumber;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
