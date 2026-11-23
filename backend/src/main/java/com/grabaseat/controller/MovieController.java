package com.grabaseat.controller;

import com.grabaseat.dto.MovieResponse;
import com.grabaseat.dto.MovieShowResponse;
import com.grabaseat.service.MovieService;
import com.grabaseat.service.ShowService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;
    private final ShowService showService;

    public MovieController(MovieService movieService, ShowService showService) {
        this.movieService = movieService;
        this.showService = showService;
    }

    @GetMapping
    public List<MovieResponse> getAllMovies() {
        return movieService.getAllMovies();
    }

    @GetMapping("/{id}")
    public MovieResponse getMovieById(@PathVariable Long id) {
        return movieService.getMovieById(id);
    }

    @GetMapping("/{movieId}/shows")
    public List<MovieShowResponse> getShowsByMovieId(@PathVariable Long movieId) {
        return showService.getShowsByMovieId(movieId);
    }
}
