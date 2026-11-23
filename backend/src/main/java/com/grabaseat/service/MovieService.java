package com.grabaseat.service;

import com.grabaseat.dto.MovieRequest;
import com.grabaseat.dto.MovieResponse;
import com.grabaseat.exception.MovieNotFoundException;
import com.grabaseat.model.Movie;
import com.grabaseat.repository.MovieRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public List<MovieResponse> getAllMovies() {
        return movieRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public MovieResponse getMovieById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found"));
        return toResponse(movie);
    }

    public MovieResponse createMovie(MovieRequest request) {
        Movie movie = new Movie(request.getTitle(), request.getLanguage(), request.getGenre(), request.getDurationMinutes());
        Movie saved = movieRepository.save(movie);
        return toResponse(saved);
    }

    public MovieResponse updateMovie(Long id, MovieRequest request) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found"));

        movie.setTitle(request.getTitle());
        movie.setLanguage(request.getLanguage());
        movie.setGenre(request.getGenre());
        movie.setDurationMinutes(request.getDurationMinutes());

        Movie updated = movieRepository.save(movie);
        return toResponse(updated);
    }

    public void deleteMovie(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found"));
        movieRepository.delete(movie);
    }

    private MovieResponse toResponse(Movie movie) {
        return new MovieResponse(movie.getId(), movie.getTitle(), movie.getLanguage(), movie.getGenre(), movie.getDurationMinutes());
    }
}
