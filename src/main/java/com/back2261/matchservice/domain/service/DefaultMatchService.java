package com.back2261.matchservice.domain.service;

import com.back2261.matchservice.infrastructure.entity.*;
import com.back2261.matchservice.infrastructure.repository.AchievementsRepository;
import com.back2261.matchservice.infrastructure.repository.AvatarsRepository;
import com.back2261.matchservice.infrastructure.repository.GamerRepository;
import com.back2261.matchservice.infrastructure.repository.GamesRepository;
import com.back2261.matchservice.interfaces.dto.GamerDto;
import com.back2261.matchservice.interfaces.dto.GamesDto;
import com.back2261.matchservice.interfaces.dto.RecommendationResponseBody;
import com.back2261.matchservice.interfaces.request.GamerRequest;
import com.back2261.matchservice.interfaces.request.SendNotificationTokenRequest;
import com.back2261.matchservice.interfaces.response.RecommendationResponse;
import feign.FeignException;
import io.github.GameBuddyDevs.backendlibrary.base.BaseBody;
import io.github.GameBuddyDevs.backendlibrary.base.Status;
import io.github.GameBuddyDevs.backendlibrary.enums.TransactionCode;
import io.github.GameBuddyDevs.backendlibrary.exception.BusinessException;
import io.github.GameBuddyDevs.backendlibrary.interfaces.DefaultMessageBody;
import io.github.GameBuddyDevs.backendlibrary.interfaces.DefaultMessageResponse;
import io.github.GameBuddyDevs.backendlibrary.service.JwtService;
import io.github.GameBuddyDevs.backendlibrary.util.Constants;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultMatchService implements MatchService {

    private final PredictFeignService predictFeignService;
    private final JwtService jwtService;
    private final GamerRepository gamerRepository;
    private final AchievementsRepository achievementsRepository;
    private final GamesRepository gamesRepository;
    private final AvatarsRepository avatarsRepository;
    private final NotificationService notificationService;

    @Override
    public RecommendationResponse getRecommendations(String token) {
        Gamer gamer = extractGamer(token);
        List<String> recUsers = doFeignPredict(gamer.getUserId());
        Set<Gamer> approvedGamers = gamer.getApprovedMatches();
        Set<Gamer> declinedGamers = gamer.getDeclinedMatches();
        for (Gamer approvedGamer : approvedGamers) {
            recUsers.remove(approvedGamer.getUserId());
        }
        for (Gamer declinedGamer : declinedGamers) {
            recUsers.remove(declinedGamer.getUserId());
        }

        List<GamerDto> recommendedGamersDto = new ArrayList<>();
        for (String recUser : recUsers) {
            Gamer recGamer = gamerRepository
                    .findById(recUser)
                    .orElseThrow(() -> new BusinessException(TransactionCode.USER_NOT_FOUND));
            GamerDto gamerDto = mapGamer(recGamer);
            recommendedGamersDto.add(gamerDto);
        }

        RecommendationResponse response = new RecommendationResponse();
        RecommendationResponseBody body = new RecommendationResponseBody();
        body.setRecommendedGamers(recommendedGamersDto);
        response.setBody(new BaseBody<>(body));
        response.setStatus(new Status(TransactionCode.DEFAULT_100));
        return response;
    }

    @Override
    public RecommendationResponse getSelectedGameRecommendations(String token, String gameId) {
        Gamer gamer = extractGamer(token);
        Games game =
                gamesRepository.findById(gameId).orElseThrow(() -> new BusinessException(TransactionCode.DB_ERROR));
        Set<Gamer> gameLovers = game.getGamers();
        gameLovers.remove(gamer);
        Set<Gamer> approvedGamers = gamer.getApprovedMatches();
        for (Gamer approvedGamer : approvedGamers) {
            gameLovers.remove(approvedGamer);
        }

        List<GamerDto> recommendedGamersDto = new ArrayList<>();
        for (Gamer recGamer : gameLovers) {
            GamerDto gamerDto = mapGamer(recGamer);
            recommendedGamersDto.add(gamerDto);
        }

        RecommendationResponse response = new RecommendationResponse();
        RecommendationResponseBody body = new RecommendationResponseBody();
        body.setRecommendedGamers(recommendedGamersDto);
        response.setBody(new BaseBody<>(body));
        response.setStatus(new Status(TransactionCode.DEFAULT_100));
        return response;
    }

    @Override
    public DefaultMessageResponse acceptGamer(String token, GamerRequest gamerRequest) {
        String email = jwtService.extractUsername(token);
        String userId = gamerRequest.getUserId();
        Gamer gamer = gamerRepository
                .findByEmail(email)
                .orElseThrow(() -> new BusinessException(TransactionCode.USER_NOT_FOUND));
        Gamer gamerToAccept = gamerRepository
                .findById(userId)
                .orElseThrow(() -> new BusinessException(TransactionCode.USER_NOT_FOUND));
        gamer.getApprovedMatches().add(gamerToAccept);
        if (gamer.getApprovedMatches().size() == 3) {
            Achievements achievements = achievementsRepository
                    .findByAchievementName("Talkative Person!!!")
                    .orElseThrow(() -> new BusinessException(TransactionCode.ACHIEVEMENT_NOT_FOUND));
            gamer.getGamerEarnedAchievements().add(achievements);
            SendNotificationTokenRequest tokenRequest = new SendNotificationTokenRequest();
            tokenRequest.setToken(gamer.getFcmToken());
            tokenRequest.setTitle(Constants.ACHIEVEMENT_TITLE);
            tokenRequest.setBody(String.format(Constants.ACHIEVEMENT_BODY, achievements.getAchievementName()));
            try {
                notificationService.sendToToken(tokenRequest);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        gamerRepository.save(gamer);
        DefaultMessageResponse response = new DefaultMessageResponse();
        DefaultMessageBody body = new DefaultMessageBody("Gamer accepted");
        response.setBody(new BaseBody<>(body));
        response.setStatus(new Status(TransactionCode.DEFAULT_100));
        return response;
    }

    @Override
    public DefaultMessageResponse declineGamer(String token, GamerRequest gamerRequest) {
        String email = jwtService.extractUsername(token);
        String userId = gamerRequest.getUserId();
        Gamer gamer = gamerRepository
                .findByEmail(email)
                .orElseThrow(() -> new BusinessException(TransactionCode.USER_NOT_FOUND));
        Gamer gamerToDecline = gamerRepository
                .findById(userId)
                .orElseThrow(() -> new BusinessException(TransactionCode.USER_NOT_FOUND));
        gamer.getDeclinedMatches().add(gamerToDecline);
        gamerRepository.save(gamer);
        DefaultMessageResponse response = new DefaultMessageResponse();
        DefaultMessageBody body = new DefaultMessageBody("Gamer declined");
        response.setBody(new BaseBody<>(body));
        response.setStatus(new Status(TransactionCode.DEFAULT_100));
        return response;
    }

    private Gamer extractGamer(String token) {
        String email = jwtService.extractUsername(token);
        Optional<Gamer> gamerOptional = gamerRepository.findByEmail(email);
        if (gamerOptional.isEmpty()) {
            throw new BusinessException(TransactionCode.USER_NOT_FOUND);
        }
        return gamerOptional.get();
    }

    private List<String> doFeignPredict(String userId) {
        List<String> recUsers;
        try {
            recUsers = predictFeignService.predict(userId).getSim_users();
        } catch (FeignException e) {
            throw new BusinessException(TransactionCode.FEIGN_SERVICE_ERROR);
        }
        return recUsers;
    }

    private List<GamesDto> mapGames(Set<Games> favoriteGames) {
        List<GamesDto> favoriteGamesDto = new ArrayList<>();
        for (Games game : favoriteGames) {
            GamesDto gamesDto = new GamesDto();
            gamesDto.setGameName(game.getGameName());
            gamesDto.setGameIcon(game.getGameIcon());
            favoriteGamesDto.add(gamesDto);
        }
        return favoriteGamesDto;
    }

    private List<String> mapKeywords(Set<Keywords> keywords) {
        List<String> keywordsDto = new ArrayList<>();
        for (Keywords keyword : keywords) {
            keywordsDto.add(keyword.getKeywordName());
        }
        return keywordsDto;
    }

    private GamerDto mapGamer(Gamer gamer) {
        GamerDto gamerDto = new GamerDto();
        BeanUtils.copyProperties(gamer, gamerDto);
        List<GamesDto> favoriteGames = mapGames(gamer.getLikedgames());
        gamerDto.setFavoriteGames(favoriteGames);
        List<String> keywords = mapKeywords(gamer.getKeywords());
        gamerDto.setSelectedKeywords(keywords);
        String avatar = avatarsRepository
                .findById(gamer.getAvatar())
                .orElse(new Avatars())
                .getImage();
        gamerDto.setAvatar(avatar);
        return gamerDto;
    }
}
