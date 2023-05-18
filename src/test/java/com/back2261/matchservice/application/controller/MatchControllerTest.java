package com.back2261.matchservice.application.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.back2261.matchservice.domain.service.DefaultMatchService;
import com.back2261.matchservice.interfaces.dto.GamerDto;
import com.back2261.matchservice.interfaces.dto.RecommendationResponseBody;
import com.back2261.matchservice.interfaces.request.GamerRequest;
import com.back2261.matchservice.interfaces.response.RecommendationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.GameBuddyDevs.backendlibrary.base.BaseBody;
import io.github.GameBuddyDevs.backendlibrary.base.Status;
import io.github.GameBuddyDevs.backendlibrary.enums.TransactionCode;
import io.github.GameBuddyDevs.backendlibrary.interfaces.DefaultMessageBody;
import io.github.GameBuddyDevs.backendlibrary.interfaces.DefaultMessageResponse;
import io.github.GameBuddyDevs.backendlibrary.service.JwtService;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(
        value = MatchController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class})
class MatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DefaultMatchService defaultMatchService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;
    private DefaultMessageResponse defaultMessageResponse;

    @BeforeEach
    void setUp() {
        token = "test";
        defaultMessageResponse = new DefaultMessageResponse();
        DefaultMessageBody defaultMessageBody = new DefaultMessageBody("test");
        defaultMessageResponse.setBody(new BaseBody<>(defaultMessageBody));
    }

    @Test
    void testGetRecommendations_whenValidTokenProvided_shouldReturnRecommendedUsers() throws Exception {
        RecommendationResponse recommendationResponse = new RecommendationResponse();
        RecommendationResponseBody body = new RecommendationResponseBody();
        List<GamerDto> recommendedGamers = new ArrayList<>();
        recommendedGamers.add(new GamerDto());
        body.setRecommendedGamers(recommendedGamers);
        recommendationResponse.setBody(new BaseBody<>(body));
        recommendationResponse.setStatus(new Status(TransactionCode.DEFAULT_100));

        Mockito.when(defaultMatchService.getRecommendations(token)).thenReturn(recommendationResponse);

        var request = MockMvcRequestBuilders.get("/match/get/recommendations")
                .contentType("application/json")
                .header("Authorization", "Bearer " + token);
        var response = mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        String responseJson = response.getResponse().getContentAsString();

        RecommendationResponse responseObj = objectMapper.readValue(responseJson, RecommendationResponse.class);
        assertEquals(200, response.getResponse().getStatus());
        assertEquals(1, responseObj.getBody().getData().getRecommendedGamers().size());
    }

    @Test
    void testGetSelectedGameRecommendations_whenValidTokenAndGameIdProvided_shouldReturnRecommendedUsers()
            throws Exception {
        RecommendationResponse recommendationResponse = new RecommendationResponse();
        RecommendationResponseBody body = new RecommendationResponseBody();
        List<GamerDto> recommendedGamers = new ArrayList<>();
        recommendedGamers.add(new GamerDto());
        body.setRecommendedGamers(recommendedGamers);
        recommendationResponse.setBody(new BaseBody<>(body));
        recommendationResponse.setStatus(new Status(TransactionCode.DEFAULT_100));

        Mockito.when(defaultMatchService.getSelectedGameRecommendations(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(recommendationResponse);

        var request = MockMvcRequestBuilders.get("/match/get/selected/game/test")
                .contentType("application/json")
                .header("Authorization", "Bearer " + token);
        var response = mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        String responseJson = response.getResponse().getContentAsString();

        RecommendationResponse responseObj = objectMapper.readValue(responseJson, RecommendationResponse.class);
        assertEquals(200, response.getResponse().getStatus());
        assertEquals(1, responseObj.getBody().getData().getRecommendedGamers().size());
    }

    @Test
    void testAcceptMatch_whenValidRequestProvided_shouldReturnSuccessMessage() throws Exception {
        GamerRequest gamerRequest = new GamerRequest();
        gamerRequest.setUserId("test");

        Mockito.when(defaultMatchService.acceptGamer(token, gamerRequest)).thenReturn(defaultMessageResponse);

        var request = MockMvcRequestBuilders.post("/match/accept")
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(gamerRequest));
        var response = mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        assertEquals(200, response.getResponse().getStatus());
    }

    @Test
    void testDeclineMatch_whenValidRequestProvided_shouldReturnSuccessMessage() throws Exception {
        GamerRequest gamerRequest = new GamerRequest();
        gamerRequest.setUserId("test");

        Mockito.when(defaultMatchService.declineGamer(token, gamerRequest)).thenReturn(defaultMessageResponse);

        var request = MockMvcRequestBuilders.post("/match/decline")
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(gamerRequest));
        var response = mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        assertEquals(200, response.getResponse().getStatus());
    }
}
