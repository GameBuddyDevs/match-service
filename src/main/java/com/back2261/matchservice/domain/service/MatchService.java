package com.back2261.matchservice.domain.service;

import com.back2261.matchservice.interfaces.request.GamerRequest;
import com.back2261.matchservice.interfaces.response.RecommendationResponse;
import io.github.GameBuddyDevs.backendlibrary.interfaces.DefaultMessageResponse;

public interface MatchService {

    RecommendationResponse getRecommendations(String token);

    RecommendationResponse getSelectedGameRecommendations(String token, String gameId);

    DefaultMessageResponse acceptGamer(String token, GamerRequest gamerRequest);

    DefaultMessageResponse declineGamer(String token, GamerRequest gamerRequest);
}
