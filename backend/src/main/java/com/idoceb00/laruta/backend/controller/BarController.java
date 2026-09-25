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
@RequestMapping("/api/bars")
@RequiredArgsConstructor
public class BarController {

    private final BarService barService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BarResponse createBar(@Valid @RequestBody BarRequest barRequest){
        return barService.createBar(barRequest);
    }

    @GetMapping("/{id}")
    public BarResponse getBarById(@PathVariable Long id){
        return barService.findById(id);
    }

    // Same endpoint for listing all items or dynamic search. Optional parameter
    @GetMapping
    public List<BarResponse> getBars(@RequestParam(required = false) String q){
        if (q == null || q.isBlank()){
            return barService.findAll();
        }

        return barService.searchBar(q);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBarById(@PathVariable Long id){
        barService.deleteById(id);
    }

    @PutMapping("/{id}")
    public BarResponse updateBar(@PathVariable Long id, @Valid @RequestBody BarRequest barRequest) {
        return barService.updateBar(id, barRequest);
    }
}
