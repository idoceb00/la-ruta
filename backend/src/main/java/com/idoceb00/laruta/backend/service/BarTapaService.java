package com.idoceb00.laruta.backend.service;

import com.idoceb00.laruta.backend.dto.BarTapaRequest;
import com.idoceb00.laruta.backend.dto.BarTapaResponse;
import com.idoceb00.laruta.backend.exception.BarNotFoundException;
import com.idoceb00.laruta.backend.exception.BarTapaAlreadyExistsException;
import com.idoceb00.laruta.backend.exception.BarTapaNotFoundException;
import com.idoceb00.laruta.backend.exception.CommunityNotFoundException;
import com.idoceb00.laruta.backend.model.Bar;
import com.idoceb00.laruta.backend.model.BarTapa;
import com.idoceb00.laruta.backend.model.Community;
import com.idoceb00.laruta.backend.model.Tapa;
import com.idoceb00.laruta.backend.repository.BarRepository;
import com.idoceb00.laruta.backend.repository.BarTapaRepository;
import com.idoceb00.laruta.backend.repository.CommunityRepository;
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
    private final CommunityRepository communityRepository;
    private final MembershipService membershipService;

    @Transactional
    public BarTapaResponse addTapaToBar(Long barId, BarTapaRequest barTapaRequest) {
        Bar bar = findBarOrThrow(barId);
        membershipService.requireMembership(bar.getCommunity().getId());
        Tapa tapa = tapaService.findOrCreate(barTapaRequest.tapaName());

        if (barTapaRepository.existsByBarIdAndTapaId(bar.getId(), tapa.getId())){
            throw new BarTapaAlreadyExistsException("Bar " + bar.getName() + " already has tapa " + tapa.getName());
        }

        return BarTapaResponse.from(barTapaRepository.save(new BarTapa(bar, tapa)));
    }

    @Transactional(readOnly = true)
    public List<BarTapaResponse> getTapasOfBar(Long barId) {
        Bar bar = findBarOrThrow(barId);
        membershipService.requireMembership(bar.getCommunity().getId());

        return barTapaRepository.findByBarId(barId).stream().map(BarTapaResponse::from).toList();
    }


    @Transactional(readOnly = true)
    public List<BarTapaResponse> getBarsWithTapa(Long communityId, Long tapaId) {
        findCommunityOrThrow(communityId);
        membershipService.requireMembership(communityId);
        tapaService.getById(tapaId);

        return barTapaRepository.findByTapaIdAndCommunityIdOrderByAverageRatingDesc(tapaId, communityId)
                .stream().map(BarTapaResponse::from).toList();
    }

    @Transactional
    public void removeTapaFromBar(Long barId, Long tapaId) {
        Bar bar = findBarOrThrow(barId);
        membershipService.requireMembership(bar.getCommunity().getId());
        BarTapa barTapa = barTapaRepository.findByBarIdAndTapaId(barId, tapaId)
                .orElseThrow(() -> new BarTapaNotFoundException("Tapa with id: " + tapaId + " not found in bar with id: " + barId));

        barTapaRepository.delete(barTapa);
    }

    private Bar findBarOrThrow(Long barId) {
        return barRepository.findById(barId)
                .orElseThrow(() -> new BarNotFoundException("Bar not found with id: " + barId));
    }

    private Community findCommunityOrThrow(Long communityId) {
        return communityRepository.findById(communityId)
                .orElseThrow(() -> new CommunityNotFoundException("Community not found with id: " + communityId));
    }
}
