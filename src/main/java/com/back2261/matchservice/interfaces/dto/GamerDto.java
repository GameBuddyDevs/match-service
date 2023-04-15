package com.back2261.matchservice.interfaces.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GamerDto {
    private String userId;
    private String username;
    private Integer age;
    private String country;
    private String gender;
    private String avatar;
    private List<GamesDto> favoriteGames;
    private List<String> selectedKeywords;
}
