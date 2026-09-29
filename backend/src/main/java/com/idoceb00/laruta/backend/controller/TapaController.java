package com.idoceb00.laruta.backend.controller;

import com.idoceb00.laruta.backend.dto.TapaResponse;
import com.idoceb00.laruta.backend.service.TapaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tapas")
@RequiredArgsConstructor
public class TapaController {

    private final TapaService tapaService;

    @GetMapping
    public List<TapaResponse> getTapas(@RequestParam(required = false) String query) {
        return tapaService.searchTapas(query);
    }
}
