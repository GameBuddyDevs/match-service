package com.back2261.matchservice.domain.service.chat;

import com.back2261.matchservice.infrastructure.entity.ChatRoom;
import com.back2261.matchservice.infrastructure.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    public String getChatId(String senderId, String receiverId, boolean createIfNotExist) {

        return chatRoomRepository
                .findBySenderAndReceiver(senderId, receiverId)
                .map(ChatRoom::getChatId)
                .orElseGet(() -> {
                    if (!createIfNotExist) {
                        return null;
                    }

                    var chatId = String.format("%s_%s", senderId, receiverId);

                    ChatRoom senderReceiver = ChatRoom.builder()
                            .chatId(chatId)
                            .sender(senderId)
                            .receiver(receiverId)
                            .build();

                    ChatRoom receiverSender = ChatRoom.builder()
                            .chatId(chatId)
                            .sender(receiverId)
                            .receiver(senderId)
                            .build();
                    chatRoomRepository.save(senderReceiver);
                    chatRoomRepository.save(receiverSender);

                    return chatId;
                });
    }
}
