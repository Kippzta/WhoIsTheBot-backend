package org.whoisthebot.DTOs;

import org.whoisthebot.entity.ChatMessage;

public record ChatMessageRespDTO(String alias, String text) {
    
    public ChatMessageRespDTO(ChatMessage chatMessage) {

        this(chatMessage.getPlayer().getAlias(), chatMessage.getText());

    }

}
