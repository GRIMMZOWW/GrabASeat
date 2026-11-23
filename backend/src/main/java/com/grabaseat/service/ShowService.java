package com.grabaseat.service;

import com.grabaseat.dto.MovieShowResponse;
import com.grabaseat.dto.ShowRequest;
import com.grabaseat.dto.ShowResponse;
import com.grabaseat.exception.MovieNotFoundException;
import com.grabaseat.exception.ShowOverlapException;
import com.grabaseat.exception.TheatreNotFoundException;
import com.grabaseat.model.Movie;
import com.grabaseat.model.Show;
import com.grabaseat.model.Theatre;
import com.grabaseat.repository.MovieRepository;
import com.grabaseat.repository.ShowRepository;
import com.grabaseat.repository.TheatreRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShowService {

    private final ShowRepository showRepository;
    private final MovieRepository movieRepository;
    private final TheatreRepository theatreRepository;

    public ShowService(ShowRepository showRepository,
                       MovieRepository movieRepository,
                       TheatreRepository theatreRepository) {
        this.showRepository = showRepository;
        this.movieRepository = movieRepository;
        this.theatreRepository = theatreRepository;
    }

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
