package com.grabaseat.repository;

import com.grabaseat.model.ShowSeat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {
    List<ShowSeat> findByShowIdOrderBySeatNumberAsc(Long showId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<ShowSeat> findByShowIdAndSeatNumberIn(Long showId, List<String> seatNumbers);
}
