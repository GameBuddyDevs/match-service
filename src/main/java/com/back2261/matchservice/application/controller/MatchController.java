package com.back2261.matchservice.application.controller;

import com.back2261.matchservice.domain.service.MatchService;
import com.back2261.matchservice.interfaces.request.GamerRequest;
import com.back2261.matchservice.interfaces.response.RecommendationResponse;
import io.github.GameBuddyDevs.backendlibrary.interfaces.DefaultMessageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/match")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    private static final String AUTHORIZATION = "Authorization";
    private static final String AUTH_MESSAGE = "Authorization field cannot be empty";

    @GetMapping("/get/recommendations")
    public ResponseEntity<RecommendationResponse> getRecommendations(
            @Valid @RequestHeader(AUTHORIZATION) @NotBlank(message = AUTH_MESSAGE) String token) {
        return new ResponseEntity<>(matchService.getRecommendations(token.substring(7)), HttpStatus.OK);
    }

    @GetMapping("/get/selected/game/{gameId}")
    public ResponseEntity<RecommendationResponse> getSelectedGameRecommendations(
            @Valid @RequestHeader(AUTHORIZATION) @NotBlank(message = AUTH_MESSAGE) String token,
            @Valid @PathVariable String gameId) {
        return new ResponseEntity<>(
                matchService.getSelectedGameRecommendations(token.substring(7), gameId), HttpStatus.OK);
    }

    @PostMapping("/accept/match")
    public ResponseEntity<DefaultMessageResponse> acceptMatch(
            @Valid @RequestHeader(AUTHORIZATION) @NotBlank(message = AUTH_MESSAGE) String token,
            @Valid @RequestBody GamerRequest gamerRequest) {
        return new ResponseEntity<>(matchService.acceptGamer(token.substring(7), gamerRequest), HttpStatus.OK);
    }

    @PostMapping("/decline/match")
    public ResponseEntity<DefaultMessageResponse> declineMatch(
            @Valid @RequestHeader(AUTHORIZATION) @NotBlank(message = AUTH_MESSAGE) String token,
            @Valid @RequestBody GamerRequest gamerRequest) {
        return new ResponseEntity<>(matchService.declineGamer(token.substring(7), gamerRequest), HttpStatus.OK);
    }
}
