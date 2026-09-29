package com.idoceb00.laruta.backend.service;

import com.idoceb00.laruta.backend.dto.BarTapaRequest;
import com.idoceb00.laruta.backend.dto.BarTapaResponse;
import com.idoceb00.laruta.backend.dto.RatingRequest;
import com.idoceb00.laruta.backend.exception.BarNotFoundException;
import com.idoceb00.laruta.backend.exception.BarTapaAlreadyExistsException;
import com.idoceb00.laruta.backend.exception.BarTapaNotFoundException;
import com.idoceb00.laruta.backend.exception.TapaNotFoundException;
import com.idoceb00.laruta.backend.model.Bar;
import com.idoceb00.laruta.backend.model.BarTapa;
import com.idoceb00.laruta.backend.model.Tapa;
import com.idoceb00.laruta.backend.repository.BarRepository;
import com.idoceb00.laruta.backend.repository.BarTapaRepository;
import com.idoceb00.laruta.backend.repository.TapaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BarTapaService {

    private final BarTapaRepository barTapaRepository;
    private final TapaService tapaService;
    private final BarRepository barRepository;

    @Transactional
    public BarTapaResponse addTapaToBar(Long barId, BarTapaRequest barTapaRequest) {
        Bar bar = barRepository.findById(barId).orElseThrow(() -> new BarNotFoundException("Bar not found with id: " + barId));
        Tapa tapa = tapaService.findOrCreate(barTapaRequest.tapaName());

        if (barTapaRepository.existsByBarIdAndTapaId(bar.getId(), tapa.getId())){
            throw new BarTapaAlreadyExistsException("Bar " + bar.getName() + " already has tapa " + tapa.getName());
        }

        return BarTapaResponse.from(barTapaRepository.save(new BarTapa(bar, tapa, barTapaRequest.rating())));
    }

    @Transactional(readOnly = true)
    public List<BarTapaResponse> getTapasOfBar(Long barId) {
        if (!barRepository.existsById(barId)){
            throw new BarNotFoundException("Bar not found with id: " + barId);
        }

        return barTapaRepository.findByBarId(barId).stream().map(BarTapaResponse::from).toList();
    }


    @Transactional(readOnly = true)
    public List<BarTapaResponse> getBarsWithTapa(Long tapaId) {
        tapaService.getById(tapaId);

        return barTapaRepository.findByTapaIdOrderByRatingDesc(tapaId).stream().map(BarTapaResponse::from).toList();
    }

    @Transactional
    public BarTapaResponse updateRating(Long barId, Long tapaId, RatingRequest ratingRequest) {
        BarTapa barTapa = barTapaRepository.findByBarIdAndTapaId(barId, tapaId).orElseThrow(() -> new BarTapaNotFoundException("Tapa with id: " + tapaId + " not found in bar with id: " + barId));

        barTapa.updateRating(ratingRequest.rating());

        return BarTapaResponse.from(barTapa);
    }

    @Transactional
    public void removeTapaFromBar(Long barId, Long tapaId) {
        BarTapa barTapa = barTapaRepository.findByBarIdAndTapaId(barId, tapaId).orElseThrow(() -> new BarTapaNotFoundException("Tapa with id: " + tapaId + " not found in bar with id: " + barId));

        barTapaRepository.delete(barTapa);
    }

}
