package com.back2261.matchservice.infrastructure.entity;

import io.github.GameBuddyDevs.backendlibrary.util.MessageStatus;
import java.io.Serializable;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "messages")
public class Message implements Serializable {

    @Id
    private String id;

    private String chatId;
    private String sender;
    private String receiver;
    private String senderName;
    private String receiverName;
    private String message;
    private String date;
    private MessageStatus status;
    private Boolean isReported = false;
}
