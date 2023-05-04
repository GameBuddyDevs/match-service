package com.back2261.matchservice.application.controller;

import com.back2261.matchservice.domain.service.chat.ChatMessageService;
import com.back2261.matchservice.domain.service.chat.ChatRoomService;
import com.back2261.matchservice.infrastructure.entity.ChatNotification;
import com.back2261.matchservice.infrastructure.entity.Message;
import com.back2261.matchservice.interfaces.response.ConversationResponse;
import com.back2261.matchservice.interfaces.response.InboxResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@Controller
@RequiredArgsConstructor
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final ChatMessageService chatMessageService;

    private final ChatRoomService chatRoomService;

    private static final String AUTHORIZATION = "Authorization";
    private static final String AUTH_MESSAGE = "Authorization field cannot be empty";

    @MessageMapping("/chat")
    public void processMessage(@Payload Message message) {
        String chatId = chatRoomService.getChatId(message.getSender(), message.getReceiver(), true);
        message.setChatId(chatId);

        Message saved = chatMessageService.save(message);
        messagingTemplate.convertAndSendToUser(
                message.getReceiver(),
                "/queue/messages",
                new ChatNotification(saved.getId(), saved.getSender(), saved.getSenderName()));
    }

    @GetMapping("/get/messages/{friendId}")
    public ResponseEntity<ConversationResponse> findChatMessages(
            @Valid @RequestHeader(AUTHORIZATION) @NotBlank(message = AUTH_MESSAGE) String token,
            @Valid @PathVariable String friendId) {
        return new ResponseEntity<>(chatMessageService.findChatMessages(token.substring(7), friendId), HttpStatus.OK);
    }

    @GetMapping("/get/inbox")
    public ResponseEntity<InboxResponse> findInbox(
            @Valid @RequestHeader(AUTHORIZATION) @NotBlank(message = AUTH_MESSAGE) String token) {
        return new ResponseEntity<>(chatMessageService.findInbox(token.substring(7)), HttpStatus.OK);
    }
}
