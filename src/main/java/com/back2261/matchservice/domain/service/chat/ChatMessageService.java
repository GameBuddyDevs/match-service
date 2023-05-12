package com.back2261.matchservice.domain.service.chat;

import com.back2261.matchservice.infrastructure.entity.Avatars;
import com.back2261.matchservice.infrastructure.entity.ChatRoom;
import com.back2261.matchservice.infrastructure.entity.Gamer;
import com.back2261.matchservice.infrastructure.entity.Message;
import com.back2261.matchservice.infrastructure.repository.AvatarsRepository;
import com.back2261.matchservice.infrastructure.repository.ChatRoomRepository;
import com.back2261.matchservice.infrastructure.repository.GamerRepository;
import com.back2261.matchservice.infrastructure.repository.MessageRepository;
import com.back2261.matchservice.interfaces.dto.ConversationDto;
import com.back2261.matchservice.interfaces.dto.ConversationResponseBody;
import com.back2261.matchservice.interfaces.dto.InboxDto;
import com.back2261.matchservice.interfaces.dto.InboxResponseBody;
import com.back2261.matchservice.interfaces.response.ConversationResponse;
import com.back2261.matchservice.interfaces.response.InboxResponse;
import com.back2261.matchservice.util.MessageStatus;
import io.github.GameBuddyDevs.backendlibrary.base.BaseBody;
import io.github.GameBuddyDevs.backendlibrary.base.Status;
import io.github.GameBuddyDevs.backendlibrary.enums.TransactionCode;
import io.github.GameBuddyDevs.backendlibrary.exception.BusinessException;
import io.github.GameBuddyDevs.backendlibrary.interfaces.DefaultMessageBody;
import io.github.GameBuddyDevs.backendlibrary.interfaces.DefaultMessageResponse;
import io.github.GameBuddyDevs.backendlibrary.service.JwtService;
import java.time.Instant;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final MessageRepository messageRepository;
    private final GamerRepository gamerRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final AvatarsRepository avatarsRepository;
    private final ChatRoomService chatRoomService;
    private final JwtService jwtService;

    @Autowired
    private MongoOperations mongoOperations;

    public Message save(Message message) {
        String senderUsername = gamerRepository
                .findById(message.getSender())
                .orElseThrow(() -> new BusinessException(TransactionCode.USER_NOT_FOUND))
                .getGamerUsername();
        String receiverUsername = gamerRepository
                .findById(message.getReceiver())
                .orElseThrow(() -> new BusinessException(TransactionCode.USER_NOT_FOUND))
                .getGamerUsername();
        message.setSenderName(senderUsername);
        message.setReceiverName(receiverUsername);
        message.setStatus(MessageStatus.RECEIVED);
        message.setId(UUID.randomUUID().toString());
        messageRepository.save(message);
        return message;
    }

    public ConversationResponse findChatMessages(String token, String friendId) {
        Gamer gamer = extractGamer(token);
        String chatId = chatRoomService.getChatId(gamer.getUserId(), friendId, false);
        List<Message> messages = new ArrayList<>();
        if (chatId != null) {
            messages = messageRepository.findByChatId(chatId);
        }
        if (messages.size() > 0) {
            updateStatuses(friendId, gamer.getUserId(), MessageStatus.DELIVERED);
        }
        List<ConversationDto> conversationDtoList = new ArrayList<>();

        for (Message message : messages) {
            ConversationDto conversationDto = new ConversationDto();
            BeanUtils.copyProperties(message, conversationDto);
            conversationDto.setDate(Date.from(Instant.parse(message.getDate())));
            conversationDtoList.add(conversationDto);
        }

        ConversationResponse conversationResponse = new ConversationResponse();
        ConversationResponseBody body = new ConversationResponseBody();
        body.setConversations(conversationDtoList);
        conversationResponse.setBody(new BaseBody<>(body));
        conversationResponse.setStatus(new Status(TransactionCode.DEFAULT_100));
        return conversationResponse;
    }

    public InboxResponse findInbox(String token) {
        Gamer gamer = extractGamer(token);
        List<ChatRoom> chatRooms = chatRoomRepository.findAllBySender(gamer.getUserId());
        List<InboxDto> inboxDtoList = new ArrayList<>();

        chatRooms.forEach(chatRoom -> {
            Message lastMessage = messageRepository.findFirstByChatIdOrderByDateDesc(chatRoom.getChatId());
            Gamer friend = gamerRepository
                    .findById(chatRoom.getReceiver())
                    .orElseThrow(() -> new BusinessException(TransactionCode.USER_NOT_FOUND));
            InboxDto inboxDto = new InboxDto();
            String image = avatarsRepository
                    .findById(friend.getAvatar())
                    .orElse(new Avatars())
                    .getImage();
            inboxDto.setAvatar(image);
            inboxDto.setUsername(friend.getGamerUsername());
            inboxDto.setUserId(friend.getUserId());
            inboxDto.setLastMessageTime(Date.from(Instant.parse(lastMessage.getDate())));
            inboxDto.setLastMessage(lastMessage.getMessage());
            inboxDto.setUnreadCount(countNewMessages(friend.getUserId(), gamer.getUserId()));
            inboxDtoList.add(inboxDto);
        });

        InboxResponse inboxResponse = new InboxResponse();
        InboxResponseBody body = new InboxResponseBody();
        body.setInboxList(inboxDtoList);
        inboxResponse.setBody(new BaseBody<>(body));
        inboxResponse.setStatus(new Status(TransactionCode.DEFAULT_100));
        return inboxResponse;
    }

    public DefaultMessageResponse reportMessage(String token, String messageId) {
        Gamer gamer = extractGamer(token);
        Message message = messageRepository
                .findById(messageId)
                .orElseThrow(() -> new BusinessException(TransactionCode.DB_ERROR));
        if (message.getReceiver().equals(gamer.getUserId())) {
            message.setIsReported(true);
            message.setMessage("*********");
            messageRepository.save(message);
        } else {
            throw new BusinessException(TransactionCode.DB_ERROR);
        }

        DefaultMessageResponse response = new DefaultMessageResponse();
        DefaultMessageBody body = new DefaultMessageBody("Message reported successfully");
        response.setBody(new BaseBody<>(body));
        response.setStatus(new Status(TransactionCode.DEFAULT_100));
        return response;
    }

    private long countNewMessages(String senderId, String friendId) {
        return messageRepository.countBySenderAndReceiverAndStatus(senderId, friendId, MessageStatus.RECEIVED);
    }

    private Gamer extractGamer(String token) {
        String email = jwtService.extractUsername(token);
        Optional<Gamer> gamerOptional = gamerRepository.findByEmail(email);
        if (gamerOptional.isEmpty()) {
            throw new BusinessException(TransactionCode.USER_NOT_FOUND);
        }
        return gamerOptional.get();
    }

    public void updateStatuses(String senderId, String receiverId, MessageStatus status) {
        Query query =
                new Query(Criteria.where("sender").is(senderId).and("receiver").is(receiverId));
        Update update = Update.update("status", status);
        mongoOperations.updateMulti(query, update, Message.class);
    }
}
