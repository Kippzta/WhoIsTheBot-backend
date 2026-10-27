package org.whoisthebot.repository;

import java.util.List;

import org.whoisthebot.entity.ChatMessage;
import org.whoisthebot.entity.GameRoom;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped 
public class ChatMessageRepository implements PanacheRepository<ChatMessage> {
    
      // Hämtar rummets senaste meddelanden, nyaste först.
    public List<ChatMessage> findLatestMessagesByGameRoom(GameRoom gameRoom, int numberOfMessages) {
        return find("gameRoom", Sort.descending("sentAt"), gameRoom)
        .page(0, numberOfMessages)
        .list();

    }

}
