package com.grabaseat.dto;

import java.time.LocalDateTime;
import java.util.List;

public class LockSeatsResponse {

    private Long showId;
    private List<String> seatNumbers;
    private LocalDateTime lockedUntil;

    public LockSeatsResponse() {
    }

    public LockSeatsResponse(Long showId, List<String> seatNumbers, LocalDateTime lockedUntil) {
        this.showId = showId;
        this.seatNumbers = seatNumbers;
        this.lockedUntil = lockedUntil;
    }

    public Long getShowId() {
        return showId;
    }

    public List<String> getSeatNumbers() {
        return seatNumbers;
    }

    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }
}
