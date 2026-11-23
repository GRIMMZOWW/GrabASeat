package com.grabaseat.dto;

import com.grabaseat.model.PaymentMethod;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class BookingRequest {

    @NotEmpty(message = "Seat numbers cannot be empty")
    @Size(min = 1, max = 6, message = "Seat numbers must have between 1 and 6 items")
    private List<String> seatNumbers;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    public BookingRequest() {
    }

    public BookingRequest(List<String> seatNumbers, PaymentMethod paymentMethod) {
        this.seatNumbers = seatNumbers;
        this.paymentMethod = paymentMethod;
    }

    public List<String> getSeatNumbers() {
        return seatNumbers;
    }

    public void setSeatNumbers(List<String> seatNumbers) {
        this.seatNumbers = seatNumbers;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
