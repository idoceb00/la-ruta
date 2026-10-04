package com.idoceb00.laruta.backend.service;

import com.idoceb00.laruta.backend.dto.BarResponse;
import com.idoceb00.laruta.backend.dto.BarRequest;
import com.idoceb00.laruta.backend.exception.BarNotFoundException;
import com.idoceb00.laruta.backend.model.Bar;
import com.idoceb00.laruta.backend.repository.BarRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BarService {

    private final BarRepository barRepository;

    @Transactional
    public BarResponse createBar(BarRequest request) {
        Bar bar = new Bar(
                request.name(),
                request.city(),
                request.address(),
                request.zone(),
                request.notes()
        );

        return BarResponse.from(barRepository.save(bar));
    }

    @Transactional(readOnly = true)
    public BarResponse findById(Long id) {
        Bar bar = barRepository.findById(id).orElseThrow(() -> new BarNotFoundException("Bar not found with id: " + id));

        return BarResponse.from(bar);
    }

    @Transactional(readOnly = true)
    public List<BarResponse> findAll() {

        return toResponses(barRepository.findAll());
    }

    @Transactional
    public void deleteById(Long id) {
        if (!barRepository.existsById(id)) {
            throw new BarNotFoundException("Bar not found with id: " + id);
        }

        barRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<BarResponse> searchBar(String query) {
        return toResponses(barRepository.search(query));
    }

    @Transactional
    public BarResponse updateBar(Long id, BarRequest request) {
        Bar bar = barRepository.findById(id).orElseThrow(()->
                new BarNotFoundException("Bar not found with id: " + id)
        );

        bar.update(request.name(), request.city(), request.address(), request.zone(), request.notes());

        return BarResponse.from(bar);
    }

    private List<BarResponse> toResponses(List<Bar> bars) {
        return bars.stream().map(BarResponse::from).toList();
    }
}
