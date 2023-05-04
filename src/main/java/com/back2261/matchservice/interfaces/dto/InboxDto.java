package com.back2261.matchservice.interfaces.dto;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InboxDto {

    private String userId;
    private String username;
    private String avatar;
    private String lastMessage;
    private Date lastMessageTime;
    private Long unreadCount;
}
