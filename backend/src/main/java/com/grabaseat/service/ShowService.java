package com.grabaseat.service;

import com.grabaseat.dto.LockSeatsRequest;
import com.grabaseat.dto.LockSeatsResponse;
import com.grabaseat.dto.MovieShowResponse;
import com.grabaseat.dto.SeatResponse;
import com.grabaseat.dto.ShowRequest;
import com.grabaseat.dto.ShowResponse;
import com.grabaseat.exception.MovieNotFoundException;
import com.grabaseat.exception.SeatUnavailableException;
import com.grabaseat.exception.ShowNotFoundException;
import com.grabaseat.exception.ShowOverlapException;
import com.grabaseat.exception.TheatreNotFoundException;
import com.grabaseat.model.Movie;
import com.grabaseat.model.SeatStatus;
import com.grabaseat.model.Show;
import com.grabaseat.model.ShowSeat;
import com.grabaseat.model.Theatre;
import com.grabaseat.model.User;
import com.grabaseat.repository.MovieRepository;
import com.grabaseat.repository.ShowRepository;
import com.grabaseat.repository.ShowSeatRepository;
import com.grabaseat.repository.TheatreRepository;
import com.grabaseat.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ShowService {

    public static final int LOCK_MINUTES = 5;
    private static final List<String> ROWS = List.of("A", "B", "C", "D", "E", "F", "G", "H");
    private static final int SEATS_PER_ROW = 10;

    private final ShowRepository showRepository;
    private final MovieRepository movieRepository;
    private final TheatreRepository theatreRepository;
    private final ShowSeatRepository showSeatRepository;
    private final UserRepository userRepository;

    public ShowService(ShowRepository showRepository,
                       MovieRepository movieRepository,
                       TheatreRepository theatreRepository,
                       ShowSeatRepository showSeatRepository,
                       UserRepository userRepository) {
        this.showRepository = showRepository;
        this.movieRepository = movieRepository;
        this.theatreRepository = theatreRepository;
        this.showSeatRepository = showSeatRepository;
        this.userRepository = userRepository;
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
                .sorted(Comparator.comparing((ShowSeat s) -> s.getSeatNumber().substring(0, 1))
                        .thenComparingInt(s -> Integer.parseInt(s.getSeatNumber().substring(1))))
                .map(seat -> {
                    SeatStatus status = seat.getStatus();
                    if (status == SeatStatus.LOCKED && seat.getLockedUntil() != null && seat.getLockedUntil().isBefore(now)) {
                        status = SeatStatus.AVAILABLE;
                    }
                    return new SeatResponse(seat.getSeatNumber(), status);
                })
                .toList();
    }

    @Transactional
    public LockSeatsResponse lockSeats(Long showId, LockSeatsRequest request, String userEmail) {
        if (request.getSeatNumbers() == null || request.getSeatNumbers().isEmpty() || request.getSeatNumbers().size() > 6) {
            throw new IllegalArgumentException("Seat numbers must contain between 1 and 6 items");
        }
        for (String seatNumber : request.getSeatNumbers()) {
            if (seatNumber == null || seatNumber.trim().isEmpty()) {
                throw new IllegalArgumentException("Seat numbers cannot contain blanks");
            }
        }
        if (request.getSeatNumbers().stream().distinct().count() != request.getSeatNumbers().size()) {
            throw new IllegalArgumentException("Duplicate seat numbers are not allowed");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Long userId = user.getId();

        if (!showRepository.existsById(showId)) {
            throw new ShowNotFoundException("Show not found");
        }

        List<ShowSeat> seats = showSeatRepository.findByShowIdAndSeatNumberIn(showId, request.getSeatNumbers());
        Set<String> foundSeatNumbers = seats.stream().map(ShowSeat::getSeatNumber).collect(Collectors.toSet());
        for (String seatNumber : request.getSeatNumbers()) {
            if (!foundSeatNumbers.contains(seatNumber)) {
                throw new IllegalArgumentException("Seat " + seatNumber + " does not exist for this show");
            }
        }

        LocalDateTime now = LocalDateTime.now();
        for (ShowSeat seat : seats) {
            boolean isAvailable = seat.getStatus() == SeatStatus.AVAILABLE;
            boolean isExpiredLock = seat.getStatus() == SeatStatus.LOCKED
                    && seat.getLockedUntil() != null
                    && seat.getLockedUntil().isBefore(now);
            boolean isSameUserLock = seat.getStatus() == SeatStatus.LOCKED
                    && userId.equals(seat.getLockedByUserId());

            if (!isAvailable && !isExpiredLock && !isSameUserLock) {
                throw new SeatUnavailableException("Seat " + seat.getSeatNumber() + " is not available");
            }
        }

        LocalDateTime lockedUntil = now.plusMinutes(LOCK_MINUTES);
        for (ShowSeat seat : seats) {
            seat.setStatus(SeatStatus.LOCKED);
            seat.setLockedByUserId(userId);
            seat.setLockedUntil(lockedUntil);
        }
        showSeatRepository.saveAll(seats);

        return new LockSeatsResponse(showId, request.getSeatNumbers(), lockedUntil);
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
