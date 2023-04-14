package com.back2261.matchservice.domain.service;

import com.back2261.matchservice.interfaces.response.RecommendationResponse;

public interface MatchService {

    RecommendationResponse getRecommendations(String token);
}
