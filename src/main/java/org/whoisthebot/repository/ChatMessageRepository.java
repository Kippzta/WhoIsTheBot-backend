package org.whoisthebot.repository;

import org.whoisthebot.entity.ChatMessage;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped 
public class ChatMessageRepository implements PanacheRepository<ChatMessage> {
    


}
