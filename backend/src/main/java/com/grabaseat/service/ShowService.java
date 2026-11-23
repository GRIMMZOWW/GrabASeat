package com.grabaseat.service;

import com.grabaseat.dto.MovieShowResponse;
import com.grabaseat.dto.SeatResponse;
import com.grabaseat.dto.ShowRequest;
import com.grabaseat.dto.ShowResponse;
import com.grabaseat.exception.MovieNotFoundException;
import com.grabaseat.exception.ShowNotFoundException;
import com.grabaseat.exception.ShowOverlapException;
import com.grabaseat.exception.TheatreNotFoundException;
import com.grabaseat.model.Movie;
import com.grabaseat.model.SeatStatus;
import com.grabaseat.model.Show;
import com.grabaseat.model.ShowSeat;
import com.grabaseat.model.Theatre;
import com.grabaseat.repository.MovieRepository;
import com.grabaseat.repository.ShowRepository;
import com.grabaseat.repository.ShowSeatRepository;
import com.grabaseat.repository.TheatreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ShowService {

    private static final List<String> ROWS = List.of("A", "B", "C", "D", "E", "F", "G", "H");
    private static final int SEATS_PER_ROW = 10;

    private final ShowRepository showRepository;
    private final MovieRepository movieRepository;
    private final TheatreRepository theatreRepository;
    private final ShowSeatRepository showSeatRepository;

    public ShowService(ShowRepository showRepository,
                       MovieRepository movieRepository,
                       TheatreRepository theatreRepository,
                       ShowSeatRepository showSeatRepository) {
        this.showRepository = showRepository;
        this.movieRepository = movieRepository;
        this.theatreRepository = theatreRepository;
        this.showSeatRepository = showSeatRepository;
    }

    @Transactional
    public ShowResponse createShow(ShowRequest request) {
        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Start time cannot be in the past");
        }

        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new MovieNotFoundException("Movie not found"));

        Theatre theatre = theatreRepository.findById(request.getTheatreId())
                .orElseThrow(() -> new TheatreNotFoundException("Theatre not found"));

        LocalDateTime endTime = request.getStartTime().plusMinutes(movie.getDurationMinutes());

        if (showRepository.existsOverlappingShow(theatre.getId(), request.getScreenNumber(), request.getStartTime(), endTime)) {
            throw new ShowOverlapException("Show overlaps with an existing show on the same screen");
        }

        Show show = new Show(movie, theatre, request.getScreenNumber(), request.getStartTime(), endTime, request.getPrice());
        Show saved = showRepository.save(show);

        List<ShowSeat> seats = new ArrayList<>();
        for (String row : ROWS) {
            for (int seatNum = 1; seatNum <= SEATS_PER_ROW; seatNum++) {
                seats.add(new ShowSeat(saved, row + seatNum, SeatStatus.AVAILABLE));
            }
        }
        showSeatRepository.saveAll(seats);

        return new ShowResponse(
                saved.getId(),
                movie.getId(),
                movie.getTitle(),
                theatre.getId(),
                theatre.getName(),
                theatre.getCity(),
                saved.getScreenNumber(),
                saved.getStartTime(),
                saved.getEndTime(),
                saved.getPrice()
        );
    }

    public ShowResponse getShowById(Long showId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ShowNotFoundException("Show not found"));

        return new ShowResponse(
                show.getId(),
                show.getMovie().getId(),
                show.getMovie().getTitle(),
                show.getTheatre().getId(),
                show.getTheatre().getName(),
                show.getTheatre().getCity(),
                show.getScreenNumber(),
                show.getStartTime(),
                show.getEndTime(),
                show.getPrice()
        );
    }

    public List<SeatResponse> getSeatsForShow(Long showId) {
        if (!showRepository.existsById(showId)) {
            throw new ShowNotFoundException("Show not found");
        }

        List<ShowSeat> seats = showSeatRepository.findByShowIdOrderBySeatNumberAsc(showId);
        LocalDateTime now = LocalDateTime.now();

        return seats.stream()
                .map(seat -> {
                    SeatStatus status = seat.getStatus();
                    if (status == SeatStatus.LOCKED && seat.getLockedUntil() != null && seat.getLockedUntil().isBefore(now)) {
                        status = SeatStatus.AVAILABLE;
                    }
                    return new SeatResponse(seat.getSeatNumber(), status);
                })
                .toList();
    }

    public List<MovieShowResponse> getShowsByMovieId(Long movieId) {
        if (!movieRepository.existsById(movieId)) {
            throw new MovieNotFoundException("Movie not found");
        }

        return showRepository.findByMovieId(movieId).stream()
                .map(s -> new MovieShowResponse(
                        s.getId(),
                        s.getTheatre().getName(),
                        s.getTheatre().getCity(),
                        s.getScreenNumber(),
                        s.getStartTime(),
                        s.getPrice()
                ))
                .toList();
    }
}
