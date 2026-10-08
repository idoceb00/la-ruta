package com.idoceb00.laruta.backend.controller;

import com.idoceb00.laruta.backend.dto.BarTapaRequest;
import com.idoceb00.laruta.backend.dto.BarTapaResponse;
import com.idoceb00.laruta.backend.service.BarTapaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BarTapaController {

    private final BarTapaService barTapaService;

    @PostMapping("/api/bars/{barId}/tapas")
    @ResponseStatus(HttpStatus.CREATED)
    public BarTapaResponse addTapaToBar(@PathVariable Long barId, @Valid @RequestBody BarTapaRequest barTapaRequest){
        return barTapaService.addTapaToBar(barId, barTapaRequest);
    }

    @GetMapping("/api/bars/{barId}/tapas")
    public List<BarTapaResponse> getBarTapas(@PathVariable Long barId) {
        return barTapaService.getTapasOfBar(barId);
    }

    @DeleteMapping("/api/bars/{barId}/tapas/{tapaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeTapaFromBar(@PathVariable Long barId, @PathVariable Long tapaId) {
        barTapaService.removeTapaFromBar(barId, tapaId);
    }

    @GetMapping("/api/tapas/{tapaId}/bars")
    public List<BarTapaResponse> getBarsWithTapa(@PathVariable Long tapaId) {
        return barTapaService.getBarsWithTapa(tapaId);
    }

}
