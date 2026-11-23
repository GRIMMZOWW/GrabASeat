package com.grabaseat.repository;

import com.grabaseat.model.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ShowRepository extends JpaRepository<Show, Long> {

    @Query("SELECT COUNT(s) > 0 FROM Show s WHERE s.theatre.id = :theatreId AND s.screenNumber = :screenNumber AND s.startTime < :newEndTime AND s.endTime > :newStartTime")
    boolean existsOverlappingShow(@Param("theatreId") Long theatreId,
                                  @Param("screenNumber") Integer screenNumber,
                                  @Param("newStartTime") LocalDateTime newStartTime,
                                  @Param("newEndTime") LocalDateTime newEndTime);

    List<Show> findByMovieId(Long movieId);
}
