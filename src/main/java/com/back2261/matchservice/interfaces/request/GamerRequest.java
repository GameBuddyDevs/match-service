package com.back2261.matchservice.interfaces.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GamerRequest {
    @NotBlank(message = "userId field cannot be empty")
    private String userId;
}
