package org.whoisthebot.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.whoisthebot.DTOs.ChatMessageRespDTO;
import org.whoisthebot.ai.AiService;
import org.whoisthebot.entity.ChatMessage;
import org.whoisthebot.entity.GameRoom;
import org.whoisthebot.entity.Player;
import org.whoisthebot.repository.ChatMessageRepository;
import org.whoisthebot.repository.GameRoomRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class BotService {

    private static final int MAX_HISTORY_MESSAGES = 10;

    @Inject
    GameRoomRepository gameRoomRepository;

    @Inject
    ChatMessageRepository chatMessageRepository;

    @Inject
    AiService aiService;

    // Låter boten svara i chatten efter att en människa skrivit.
    // Returnerar ett tomt Optional om boten inte ska säga något.
    @Transactional
    public Optional<ChatMessageRespDTO> generateBotReply(String roomCode) {

        GameRoom gameRoom = gameRoomRepository.findByRoomCode(roomCode.toUpperCase())
                .orElseThrow(() -> new NotFoundException("Inget spelrum hittades"));

        Player bot = null;
        // Letar fram vem som är boten i rummet
        for (Player player : gameRoom.getPlayers()) {

            if (player.isBot()) {

                bot = player;
                break;
            }

        }

        // Om boten inte finns i rummet, svara inte
        if (bot == null) {
            return Optional.empty();
        }

        // Hämtar de senaste meddelandena i chatten
        List<ChatMessage> latestChatMessages = chatMessageRepository.findLatestMessagesByGameRoom(gameRoom,
                MAX_HISTORY_MESSAGES);

        // Vänder listan så att de nyaste meddelandena är sist, annars så blir
        // chatthistoriken i fel ordning för ain
        Collections.reverse(latestChatMessages);

        // Om det inte finns några meddelanden i chatten, svara inte
        if (latestChatMessages.isEmpty()) {
            return Optional.empty();
        }

        // Hämtar det senaste meddelandet i chatten
        ChatMessage lastMessage = latestChatMessages.getLast();

        // Om sista meddelandet var från boten, svara inte
        // gör så att botten inte svarar på sig själv hela tiden
        if (lastMessage.getPlayer().isBot()) {
            return Optional.empty();
        }

        // Gör om meddelande listan till en text(sträng) en rad per meddelande, "alias: text".
       // Detta fyller i platshållaren i ai prompten {chatHistory} och formaterar strängen så det ska se ut som en chatt
       // så ain förstår sin sitation.
        StringBuilder chatHistory = new StringBuilder();

        for (ChatMessage chatMessage : latestChatMessages) {

            chatHistory.append(chatMessage.getPlayer().getAlias())
                    .append(": ")
                    .append(chatMessage.getText()).append("\n");
        }

        // Anropar ain för att få ett svar från boten, skickar med botens alias, frågan
        // som ställs i spelet och chatthistoriken
        String reply = aiService.reply(bot.getAlias(), gameRoom.getQuestion().getText(), chatHistory.toString());

        // Om ain inte svarar, returnera ett tomt Optional
        if (reply == null) {
            return Optional.empty();
        }

        // Trimma bort mellanrum från början och slutet av svaret
        String replyTrimmed = reply.trim();

        // Om svaret är tomt eller börjar med "PASS" som det står i prompten.
        if (replyTrimmed.isBlank() || replyTrimmed.toUpperCase().startsWith("PASS")) {
            return Optional.empty();
        }

        // Om svaret är längre än maxlängden, trimma det till maxlängden
        if (replyTrimmed.length() > ChatMessage.MAX_TEXT_LENGTH) {
            replyTrimmed = replyTrimmed.substring(0, ChatMessage.MAX_TEXT_LENGTH);
        }

        // Skapar ett nytt meddelande från boten och sparar det i databasen
        ChatMessage botReply = new ChatMessage(replyTrimmed, bot, gameRoom);
        chatMessageRepository.persist(botReply);

        // Returnerar DTOn med botens svar, som skickas till frontend
        return Optional.of(new ChatMessageRespDTO(botReply));

    }

}
