package com.grabaseat.dto;

import com.grabaseat.model.SeatStatus;

public class SeatResponse {

    private String seatNumber;
    private SeatStatus status;

    public SeatResponse() {
    }

    public SeatResponse(String seatNumber, SeatStatus status) {
        this.seatNumber = seatNumber;
        this.status = status;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public void setStatus(SeatStatus status) {
        this.status = status;
    }
}
