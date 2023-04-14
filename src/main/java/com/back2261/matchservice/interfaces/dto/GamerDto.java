package com.back2261.matchservice.interfaces.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GamerDto {
    private String username;
    private Integer age;
    private String country;
    private String gender;
    private byte[] avatar;
    private List<GamesDto> favoriteGames;
    private List<String> selectedKeywords;
}
