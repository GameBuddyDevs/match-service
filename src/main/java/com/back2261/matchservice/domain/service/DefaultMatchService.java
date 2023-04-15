package com.back2261.matchservice.domain.service;

import com.back2261.matchservice.infrastructure.entity.Gamer;
import com.back2261.matchservice.infrastructure.entity.Games;
import com.back2261.matchservice.infrastructure.entity.Keywords;
import com.back2261.matchservice.infrastructure.repository.GamerRepository;
import com.back2261.matchservice.interfaces.dto.GamerDto;
import com.back2261.matchservice.interfaces.dto.GamesDto;
import com.back2261.matchservice.interfaces.dto.RecommendationResponseBody;
import com.back2261.matchservice.interfaces.request.GamerRequest;
import com.back2261.matchservice.interfaces.response.RecommendationResponse;
import feign.FeignException;
import io.github.GameBuddyDevs.backendlibrary.base.BaseBody;
import io.github.GameBuddyDevs.backendlibrary.base.Status;
import io.github.GameBuddyDevs.backendlibrary.enums.TransactionCode;
import io.github.GameBuddyDevs.backendlibrary.exception.BusinessException;
import io.github.GameBuddyDevs.backendlibrary.interfaces.DefaultMessageBody;
import io.github.GameBuddyDevs.backendlibrary.interfaces.DefaultMessageResponse;
import io.github.GameBuddyDevs.backendlibrary.service.JwtService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultMatchService implements MatchService {

    private final PredictFeignService predictFeignService;
    private final JwtService jwtService;
    private final GamerRepository gamerRepository;

    @Override
    public RecommendationResponse getRecommendations(String token) {
        String email = jwtService.extractUsername(token);
        Gamer gamer = gamerRepository
                .findByEmail(email)
                .orElseThrow(() -> new BusinessException(TransactionCode.USER_NOT_FOUND));
        List<String> recUsers = doFeignPredict(gamer.getUserId());
        Set<Gamer> approvedGamers = gamer.getApprovedMatches();
        Set<Gamer> declinedGamers = gamer.getDeclinedMatches();
        for (Gamer approvedGamer : approvedGamers) {
            recUsers.remove(approvedGamer.getUserId());
        }
        for (Gamer declinedGamer : declinedGamers) {
            recUsers.remove(declinedGamer.getUserId());
        }

        List<Gamer> recommendedGamers = new ArrayList<>();
        List<GamerDto> recommendedGamersDto = new ArrayList<>();
        for (String recUser : recUsers) {
            recommendedGamers.add(gamerRepository.findById(recUser).get());
        }
        for (Gamer recGamer : recommendedGamers) {
            GamerDto gamerDto = new GamerDto();
            gamerDto.setUserId(recGamer.getUserId());
            gamerDto.setAge(recGamer.getAge());
            gamerDto.setCountry(recGamer.getCountry());
            gamerDto.setGender(recGamer.getGender());
            gamerDto.setAvatar(recGamer.getAvatar());
            gamerDto.setUsername(recGamer.getGamerUsername());
            List<GamesDto> favoriteGames = mapGames(recGamer.getLikedgames());
            gamerDto.setFavoriteGames(favoriteGames);
            List<String> keywords = mapKeywords(recGamer.getKeywords());
            gamerDto.setSelectedKeywords(keywords);
            recommendedGamersDto.add(gamerDto);
        }

        RecommendationResponse response = new RecommendationResponse();
        RecommendationResponseBody body = new RecommendationResponseBody();
        body.setRecommendedGamers(recommendedGamersDto);
        response.setBody(new BaseBody<>(body));
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

    private List<String> doFeignPredict(String userId) {
        List<String> recUsers = null;
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
}
