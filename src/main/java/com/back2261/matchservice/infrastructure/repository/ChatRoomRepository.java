package com.back2261.matchservice.infrastructure.repository;

import com.back2261.matchservice.infrastructure.entity.ChatRoom;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {

    Optional<ChatRoom> findBySenderAndReceiver(String sender, String receiver);

    List<ChatRoom> findAllBySender(String sender);
}
