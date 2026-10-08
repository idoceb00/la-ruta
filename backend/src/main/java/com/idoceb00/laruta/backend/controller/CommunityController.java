package com.idoceb00.laruta.backend.controller;

import com.idoceb00.laruta.backend.dto.CommunityRequest;
import com.idoceb00.laruta.backend.dto.CommunityResponse;
import com.idoceb00.laruta.backend.dto.JoinCommunityRequest;
import com.idoceb00.laruta.backend.service.CommunityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/communities")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommunityResponse createCommunity(@Valid @RequestBody CommunityRequest request) {
        return communityService.createCommunity(request);
    }

    @GetMapping
    public List<CommunityResponse> getMyCommunities() {
        return communityService.findMyCommunities();
    }

    @GetMapping("/{communityId}")
    public CommunityResponse getCommunity(@PathVariable Long communityId) {
        return communityService.findById(communityId);
    }

    @PostMapping("/join")
    public CommunityResponse joinCommunity(@Valid @RequestBody JoinCommunityRequest request) {
        return communityService.join(request);
    }

    @DeleteMapping("/{communityId}/members/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leaveCommunity(@PathVariable Long communityId) {
        communityService.leaveCommunity(communityId);
    }
}
