package com.back2261.matchservice.infrastructure.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "chatrooms")
@Builder
public class ChatRoom {

    @Id
    private String id;

    private String chatId;
    private String sender;
    private String receiver;
}
