package com.grabaseat.dto;

import com.grabaseat.model.BookingStatus;
import com.grabaseat.model.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class BookingResponse {

    private Long bookingId;
    private String movieTitle;
    private String theatreName;
    private String city;
    private Integer screenNumber;
    private LocalDateTime startTime;
    private List<String> seatNumbers;
    private BigDecimal totalAmount;
    private PaymentMethod paymentMethod;
    private BookingStatus status;
    private LocalDateTime createdAt;

    public BookingResponse() {
    }

    public BookingResponse(Long bookingId, String movieTitle, String theatreName, String city,
                           Integer screenNumber, LocalDateTime startTime, List<String> seatNumbers,
                           BigDecimal totalAmount, PaymentMethod paymentMethod, BookingStatus status,
                           LocalDateTime createdAt) {
        this.bookingId = bookingId;
        this.movieTitle = movieTitle;
        this.theatreName = theatreName;
        this.city = city;
        this.screenNumber = screenNumber;
        this.startTime = startTime;
        this.seatNumbers = seatNumbers;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public String getMovieTitle() {
        return movieTitle;
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

    public List<String> getSeatNumbers() {
        return seatNumbers;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
