package com.idoceb00.laruta.backend.controller;

import com.idoceb00.laruta.backend.dto.BarRequest;
import com.idoceb00.laruta.backend.dto.BarResponse;
import com.idoceb00.laruta.backend.service.BarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BarController {

    private final BarService barService;

    @PostMapping("/api/communities/{communityId}/bars")
    @ResponseStatus(HttpStatus.CREATED)
    public BarResponse createBar(@PathVariable Long communityId, @Valid @RequestBody BarRequest barRequest) {
        return barService.createBar(communityId, barRequest);
    }

    // Same endpoint for listing all items or dynamic search. Optional parameter
    @GetMapping("/api/communities/{communityId}/bars")
    public List<BarResponse> getBars(@PathVariable Long communityId, @RequestParam(required = false) String query) {
        return barService.getBars(communityId, query);
    }

    @GetMapping("/api/bars/{id}")
    public BarResponse getBarById(@PathVariable Long id) {
        return barService.findById(id);
    }

    @PutMapping("/api/bars/{id}")
    public BarResponse updateBar(@PathVariable Long id, @Valid @RequestBody BarRequest barRequest) {
        return barService.updateBar(id, barRequest);
    }

    @DeleteMapping("/api/bars/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBarById(@PathVariable Long id) {
        barService.deleteById(id);
    }
}
