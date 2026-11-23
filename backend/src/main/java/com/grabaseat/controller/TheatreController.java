package com.grabaseat.controller;

import com.grabaseat.dto.TheatreResponse;
import com.grabaseat.service.TheatreService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/theatres")
public class TheatreController {

    private final TheatreService theatreService;

    public TheatreController(TheatreService theatreService) {
        this.theatreService = theatreService;
    }

    @GetMapping
    public List<TheatreResponse> getAllTheatres() {
        return theatreService.getAllTheatres();
    }
}
