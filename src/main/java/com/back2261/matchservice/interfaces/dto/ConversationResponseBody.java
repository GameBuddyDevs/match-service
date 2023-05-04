package com.back2261.matchservice.interfaces.dto;

import io.github.GameBuddyDevs.backendlibrary.base.BaseModel;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConversationResponseBody extends BaseModel {
    private List<ConversationDto> conversations;
}
