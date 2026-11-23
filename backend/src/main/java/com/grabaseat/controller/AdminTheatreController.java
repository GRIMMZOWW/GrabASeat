package com.grabaseat.controller;

import com.grabaseat.dto.TheatreRequest;
import com.grabaseat.dto.TheatreResponse;
import com.grabaseat.service.TheatreService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/theatres")
public class AdminTheatreController {

    private final TheatreService theatreService;

    public AdminTheatreController(TheatreService theatreService) {
        this.theatreService = theatreService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TheatreResponse createTheatre(@Valid @RequestBody TheatreRequest request) {
        return theatreService.createTheatre(request);
    }
}
