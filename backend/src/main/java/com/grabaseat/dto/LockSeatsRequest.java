package com.grabaseat.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public class LockSeatsRequest {

    @NotEmpty(message = "Seat numbers cannot be empty")
    @Size(min = 1, max = 6, message = "Seat numbers must have between 1 and 6 items")
    private List<String> seatNumbers;

    public LockSeatsRequest() {
    }

    public LockSeatsRequest(List<String> seatNumbers) {
        this.seatNumbers = seatNumbers;
    }

    public List<String> getSeatNumbers() {
        return seatNumbers;
    }

    public void setSeatNumbers(List<String> seatNumbers) {
        this.seatNumbers = seatNumbers;
    }
}
