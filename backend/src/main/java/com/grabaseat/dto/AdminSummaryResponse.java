package com.grabaseat.dto;

import java.math.BigDecimal;

public class AdminSummaryResponse {

    private long confirmedBookings;
    private long cancelledBookings;
    private long seatsSold;
    private BigDecimal totalRevenue;

    public AdminSummaryResponse() {
    }

    public AdminSummaryResponse(long confirmedBookings, long cancelledBookings, long seatsSold, BigDecimal totalRevenue) {
        this.confirmedBookings = confirmedBookings;
        this.cancelledBookings = cancelledBookings;
        this.seatsSold = seatsSold;
        this.totalRevenue = totalRevenue;
    }

    public long getConfirmedBookings() {
        return confirmedBookings;
    }

    public long getCancelledBookings() {
        return cancelledBookings;
    }

    public long getSeatsSold() {
        return seatsSold;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }
}
