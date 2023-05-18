package com.back2261.matchservice.infrastructure.repository;

import com.back2261.matchservice.infrastructure.entity.Message;
import io.github.GameBuddyDevs.backendlibrary.util.MessageStatus;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {

    List<Message> findByChatId(String chatId);

    Long countBySenderAndReceiverAndStatus(String sender, String receiver, MessageStatus status);

    Message findFirstByChatIdOrderByDateDesc(String chatId);
}
