package com.back2261.matchservice.domain.service;

import static org.junit.jupiter.api.Assertions.*;

import com.back2261.matchservice.infrastructure.entity.Achievements;
import com.back2261.matchservice.infrastructure.entity.Gamer;
import com.back2261.matchservice.infrastructure.entity.Games;
import com.back2261.matchservice.infrastructure.entity.Keywords;
import com.back2261.matchservice.infrastructure.repository.AchievementsRepository;
import com.back2261.matchservice.infrastructure.repository.GamerRepository;
import com.back2261.matchservice.interfaces.request.GamerRequest;
import com.back2261.matchservice.interfaces.response.PredictFeignResponse;
import com.back2261.matchservice.interfaces.response.RecommendationResponse;
import feign.FeignException;
import feign.Request;
import io.github.GameBuddyDevs.backendlibrary.exception.BusinessException;
import io.github.GameBuddyDevs.backendlibrary.interfaces.DefaultMessageResponse;
import io.github.GameBuddyDevs.backendlibrary.service.JwtService;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DefaultMatchServiceTest {

    @InjectMocks
    private DefaultMatchService defaultMatchService;

    @Mock
    private PredictFeignService predictFeignService;

    @Mock
    private JwtService jwtService;

    @Mock
    private GamerRepository gamerRepository;

    @Mock
    private AchievementsRepository achievementsRepository;

    private String token;

    @BeforeEach
    void setUp() {
        token = "test";
    }

    @Test
    void testGetRecommendations_whenUserNotFound_ReturnErrorCode103() {
        Mockito.when(jwtService.extractUsername(Mockito.anyString())).thenReturn("test");
        Mockito.when(gamerRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.empty());

        BusinessException businessException =
                assertThrows(BusinessException.class, () -> defaultMatchService.getRecommendations(token));
        assertEquals(103, businessException.getTransactionCode().getId());
    }

    @Test
    void testGetRecommendations_whenFeignErrorOccur_ReturnErrorCode123() {
        Gamer gamer = getGamer();
        Map<String, Collection<String>> headers = new HashMap<>();
        Request request = Request.create(Request.HttpMethod.GET, "test", headers, new byte[0], null, null);
        FeignException feignException = new FeignException.BadRequest("test", request, new byte[0], headers);

        Mockito.when(jwtService.extractUsername(Mockito.anyString())).thenReturn("test");
        Mockito.when(gamerRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(gamer));
        Mockito.doThrow(feignException).when(predictFeignService).predict(Mockito.anyString());

        BusinessException businessException =
                assertThrows(BusinessException.class, () -> defaultMatchService.getRecommendations(token));
        assertEquals(123, businessException.getTransactionCode().getId());
    }

    @Test
    void testGetRecommendations_whenRecUserNotFound_ReturnErrorCode103() {
        Gamer gamer = getGamer();
        List<String> userIds = new ArrayList<>();
        userIds.add("test2");
        userIds.add("test3");
        userIds.add("test4");
        userIds.add("test5");
        userIds.add("test6");
        userIds.add("test7");
        PredictFeignResponse predictFeignResponse = new PredictFeignResponse();
        predictFeignResponse.setSim_users(userIds);
        predictFeignResponse.setUser_id("test");

        Mockito.when(jwtService.extractUsername(Mockito.anyString())).thenReturn("test");
        Mockito.when(gamerRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(gamer));
        Mockito.when(predictFeignService.predict(Mockito.anyString())).thenReturn(predictFeignResponse);
        Mockito.when(gamerRepository.findById(Mockito.anyString())).thenReturn(Optional.empty());

        BusinessException businessException =
                assertThrows(BusinessException.class, () -> defaultMatchService.getRecommendations(token));
        assertEquals(103, businessException.getTransactionCode().getId());
    }

    @Test
    void testGetRecommendations_whenTokenAndFeignServiceOk_ReturnRecommendedGamers() {
        Gamer gamer = getGamer();
        List<String> userIds = new ArrayList<>();
        userIds.add("test2");
        userIds.add("test3");
        userIds.add("test4");
        userIds.add("test5");
        userIds.add("test6");
        userIds.add("test7");
        PredictFeignResponse predictFeignResponse = new PredictFeignResponse();
        predictFeignResponse.setSim_users(userIds);
        predictFeignResponse.setUser_id("test");
        Gamer gamer2 = new Gamer();
        gamer2.setUserId("test2");
        Gamer gamer3 = new Gamer();
        gamer3.setUserId("test3");
        Gamer gamer4 = new Gamer();
        gamer4.setUserId("test4");
        Gamer gamer5 = new Gamer();
        gamer5.setUserId("test5");
        gamer.getApprovedMatches().add(gamer2);
        gamer.getApprovedMatches().add(gamer3);
        gamer.getDeclinedMatches().add(gamer4);
        gamer.getDeclinedMatches().add(gamer5);

        Mockito.when(jwtService.extractUsername(Mockito.anyString())).thenReturn("test");
        Mockito.when(gamerRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(gamer));
        Mockito.when(predictFeignService.predict(Mockito.anyString())).thenReturn(predictFeignResponse);
        Mockito.when(gamerRepository.findById(Mockito.anyString())).thenReturn(Optional.of(gamer));

        RecommendationResponse result = defaultMatchService.getRecommendations(token);
        assertEquals(2, result.getBody().getData().getRecommendedGamers().size());
        assertEquals("100", result.getStatus().getCode());
    }

    @Test
    void testAcceptGamer_whenTokenInvalid_ReturnErrorCode103() {
        GamerRequest gamerRequest = new GamerRequest();
        gamerRequest.setUserId("test2");

        Mockito.when(jwtService.extractUsername(Mockito.anyString())).thenReturn("test");
        Mockito.when(gamerRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.empty());

        BusinessException businessException =
                assertThrows(BusinessException.class, () -> defaultMatchService.acceptGamer(token, gamerRequest));
        assertEquals(103, businessException.getTransactionCode().getId());
    }

    @Test
    void testAcceptGamer_whenIdInvalid_ReturnErrorCode103() {
        Gamer gamer = getGamer();
        GamerRequest gamerRequest = new GamerRequest();
        gamerRequest.setUserId("test2");

        Mockito.when(jwtService.extractUsername(Mockito.anyString())).thenReturn("test");
        Mockito.when(gamerRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(gamer));
        Mockito.when(gamerRepository.findById(Mockito.anyString())).thenReturn(Optional.empty());

        BusinessException businessException =
                assertThrows(BusinessException.class, () -> defaultMatchService.acceptGamer(token, gamerRequest));
        assertEquals(103, businessException.getTransactionCode().getId());
    }

    @Test
    void testAcceptGamer_whenAchievementEarned_ReturnSuccess() {
        Gamer gamer = getGamer();
        GamerRequest gamerRequest = new GamerRequest();
        gamerRequest.setUserId("test2");
        gamer.getApprovedMatches().add(new Gamer());
        gamer.getApprovedMatches().add(new Gamer());
        Achievements achievements = new Achievements();
        achievements.setAchievementName("test");
        achievements.setDescription("test");
        achievements.setValue(10);
        achievements.setId(UUID.randomUUID());

        Mockito.when(jwtService.extractUsername(Mockito.anyString())).thenReturn("test");
        Mockito.when(gamerRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(gamer));
        Mockito.when(gamerRepository.findById(Mockito.anyString())).thenReturn(Optional.of(new Gamer()));
        Mockito.when(achievementsRepository.findByAchievementName(Mockito.anyString()))
                .thenReturn(Optional.of(achievements));

        DefaultMessageResponse result = defaultMatchService.acceptGamer(token, gamerRequest);
        assertEquals("100", result.getStatus().getCode());
        assertEquals(3, gamer.getApprovedMatches().size());
        assertEquals(1, gamer.getGamerEarnedAchievements().size());
    }

    @Test
    void testAcceptGamer_whenValidIdProvided_ReturnSuccess() {
        Gamer gamer = getGamer();
        GamerRequest gamerRequest = new GamerRequest();
        gamerRequest.setUserId("test2");

        Mockito.when(jwtService.extractUsername(Mockito.anyString())).thenReturn("test");
        Mockito.when(gamerRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(gamer));
        Mockito.when(gamerRepository.findById(Mockito.anyString())).thenReturn(Optional.of(new Gamer()));

        DefaultMessageResponse result = defaultMatchService.acceptGamer(token, gamerRequest);
        assertEquals("100", result.getStatus().getCode());
        assertEquals(1, gamer.getApprovedMatches().size());
    }

    @Test
    void testDeclineGamer_whenTokenInvalid_ReturnErrorCode103() {
        GamerRequest gamerRequest = new GamerRequest();
        gamerRequest.setUserId("test2");

        Mockito.when(jwtService.extractUsername(Mockito.anyString())).thenReturn("test");
        Mockito.when(gamerRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.empty());

        BusinessException businessException =
                assertThrows(BusinessException.class, () -> defaultMatchService.declineGamer(token, gamerRequest));
        assertEquals(103, businessException.getTransactionCode().getId());
    }

    @Test
    void testDeclineGamer_whenValidIdProvided_ReturnErrorCode103() {
        Gamer gamer = getGamer();
        GamerRequest gamerRequest = new GamerRequest();
        gamerRequest.setUserId("test2");

        Mockito.when(jwtService.extractUsername(Mockito.anyString())).thenReturn("test");
        Mockito.when(gamerRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(gamer));
        Mockito.when(gamerRepository.findById(Mockito.anyString())).thenReturn(Optional.empty());

        BusinessException businessException =
                assertThrows(BusinessException.class, () -> defaultMatchService.declineGamer(token, gamerRequest));
        assertEquals(103, businessException.getTransactionCode().getId());
    }

    @Test
    void testDeclineGamer_whenValidIdProvided_ReturnSuccess() {
        Gamer gamer = getGamer();
        GamerRequest gamerRequest = new GamerRequest();
        gamerRequest.setUserId("test2");

        Mockito.when(jwtService.extractUsername(Mockito.anyString())).thenReturn("test");
        Mockito.when(gamerRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(gamer));
        Mockito.when(gamerRepository.findById(Mockito.anyString())).thenReturn(Optional.of(new Gamer()));

        DefaultMessageResponse result = defaultMatchService.declineGamer(token, gamerRequest);
        assertEquals("100", result.getStatus().getCode());
        assertEquals(1, gamer.getDeclinedMatches().size());
    }

    private Gamer getGamer() {
        Gamer gamer = new Gamer();
        gamer.setUserId("test");
        gamer.setGamerUsername("test");
        gamer.setEmail("test");
        gamer.setAge(15);
        gamer.setCountry("test");
        gamer.setAvatar(UUID.randomUUID());
        gamer.setLastModifiedDate(new Date());
        gamer.setPwd("test");
        gamer.setGender("E");

        Games games = new Games();
        games.setGameId("test");
        Games games2 = new Games();
        games2.setGameId("test2");
        gamer.setLikedgames(new HashSet<>());
        gamer.getLikedgames().add(games);
        gamer.getLikedgames().add(games2);

        Keywords keywords = new Keywords();
        keywords.setId(UUID.randomUUID());
        Keywords keywords2 = new Keywords();
        keywords2.setId(UUID.randomUUID());
        gamer.setKeywords(new HashSet<>());
        gamer.getKeywords().add(keywords);
        gamer.getKeywords().add(keywords2);

        gamer.setApprovedMatches(new HashSet<>());
        gamer.setDeclinedMatches(new HashSet<>());
        gamer.setGamerEarnedAchievements(new HashSet<>());
        return gamer;
    }
}
