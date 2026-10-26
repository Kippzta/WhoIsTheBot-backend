package org.whoisthebot.service;

import java.util.UUID;

import org.whoisthebot.DTOs.ChatMessageRespDTO;
import org.whoisthebot.entity.ChatMessage;
import org.whoisthebot.entity.GameRoom;
import org.whoisthebot.entity.Player;
import org.whoisthebot.enums.GameStatus;
import org.whoisthebot.repository.ChatMessageRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;

@ApplicationScoped 
public class ChatService {
    
    @Inject 
    GameRoomService gameRoomService;

    @Inject 
    ChatMessageRepository chatMessageRepository;

    @Transactional 
    public ChatMessageRespDTO sendChatMessage(String roomCode, UUID playerId, String text) {

        Player player = gameRoomService.getPlayerFromCorrectRoom(roomCode, playerId);

       GameRoom gameRoom = player.getGameRoom();

        gameRoomService.validateCorrectGameStatus(gameRoom, GameStatus.CHATTING , "Chatten är inte öppen ännu");

        if (text == null || text.isBlank()) {
             throw new BadRequestException("Du måste skriva något innan du skickar");
        }

        String trimmedText = text.trim();

        if (trimmedText.length() > ChatMessage.MAX_TEXT_LENGTH) {
            throw new BadRequestException("Du behöver inte skriva en novell. (MAX " + ChatMessage.MAX_TEXT_LENGTH + " tecken)");
        }

        ChatMessage chatMessage = new ChatMessage(trimmedText, player, gameRoom);
        
        chatMessageRepository.persist(chatMessage);

        ChatMessageRespDTO chatMessageRespDTO = new ChatMessageRespDTO(chatMessage);

        return chatMessageRespDTO; 
    }

}
