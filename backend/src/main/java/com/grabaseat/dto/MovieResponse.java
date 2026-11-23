package com.grabaseat.dto;

public class MovieResponse {

    private Long id;
    private String title;
    private String language;
    private String genre;
    private Integer durationMinutes;

    public MovieResponse() {
    }

    public MovieResponse(Long id, String title, String language, String genre, Integer durationMinutes) {
        this.id = id;
        this.title = title;
        this.language = language;
        this.genre = genre;
        this.durationMinutes = durationMinutes;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getLanguage() {
        return language;
    }

    public String getGenre() {
        return genre;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }
}
