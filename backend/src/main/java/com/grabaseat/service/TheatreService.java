package com.grabaseat.service;

import com.grabaseat.dto.TheatreRequest;
import com.grabaseat.dto.TheatreResponse;
import com.grabaseat.model.Theatre;
import com.grabaseat.repository.TheatreRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TheatreService {

    private final TheatreRepository theatreRepository;

    public TheatreService(TheatreRepository theatreRepository) {
        this.theatreRepository = theatreRepository;
    }

    public List<TheatreResponse> getAllTheatres() {
        return theatreRepository.findAll().stream()
                .map(t -> new TheatreResponse(t.getId(), t.getName(), t.getCity()))
                .toList();
    }

    public TheatreResponse createTheatre(TheatreRequest request) {
        Theatre theatre = new Theatre(request.getName(), request.getCity());
        Theatre saved = theatreRepository.save(theatre);
        return new TheatreResponse(saved.getId(), saved.getName(), saved.getCity());
    }
}
