package org.whoisthebot.websocket;

import java.util.Optional;
import java.util.UUID;

import org.whoisthebot.DTOs.ChatMessageRespDTO;
import org.whoisthebot.DTOs.GameRoomDTO;
import org.whoisthebot.DTOs.WebSocketMessageDTO;
import org.whoisthebot.enums.GameStatus;
import org.whoisthebot.enums.WebSocketMessageType;
import org.whoisthebot.service.BotService;
import org.whoisthebot.service.ChatService;
import org.whoisthebot.service.GameRoomService;

import io.quarkus.logging.Log;
import io.quarkus.websockets.next.OnClose;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.inject.Inject;

@WebSocket(path = "/ws/rooms/{roomCode}/{playerId}")
public class GameRoomSocket {

    @Inject
    GameRoomService gameRoomService;

    @Inject
    WebSocketConnection connection;

    @Inject
    ChatService chatService;

    @Inject
    BotService botService;

    // Körs när en spelare ansluter till websocketen
    @OnOpen
    public void onOpen() {

        // Hämtar path parametrar från URL:en och sätter dem till variabler
        String roomCode = connection.pathParam("roomCode");

        try {

            UUID playerId = getPlayerId(connection);

            // Validerar spelarens id och rumskoden så rätt spelare finns och tillhör rätt
            // rum, returnerar rummet som en DTO för att
            // kunna skicka det till frontend.
            GameRoomDTO gameRoomDTO = gameRoomService.validatePlayerAndGetRoom(roomCode, playerId);

            Log.info(playerId + " " + "Har anslutit till spelrum: " + roomCode);

            // Skapar ett meddelande som skickas till alla spelare i rummet för att
            // uppdatera spelarlistan i frontend.
            WebSocketMessageDTO<GameRoomDTO> message = new WebSocketMessageDTO<>(WebSocketMessageType.ROOM_UPDATED,
                    gameRoomDTO);

            sendToRoom(roomCode, message);
        }
        // Fångar om något går fel och stänger anslutningen
        catch (Exception e) {

            Log.info("Anlutning misslyckad: " + e.getMessage());
            connection.closeAndAwait();
        }

    }

    @OnClose
    public void onClose() {
        Log.info("Någon har lämnat rummet!");
    }

    // Körs varje gång ett meddelande tas emot från frontend, type avgör vad som ska
    // göras t.ex. starta spelet.
    // data är object för vi kommer ta emot olika typer som inte är förbestämda
    @OnTextMessage
    public void onMessage(WebSocketMessageDTO<Object> webSocketMessageDTO) {

        String roomCode = connection.pathParam("roomCode");

        try {
            UUID playerId = getPlayerId(connection);

            switch (webSocketMessageDTO.type()) {
                case START_GAME:

                    GameStatus gameStatus = gameRoomService.startGame(roomCode, playerId);

                    String questionText = gameRoomService.getQuestionText(roomCode);

                    WebSocketMessageDTO<String> questionMessage = new WebSocketMessageDTO<>(
                            WebSocketMessageType.QUESTION_ASSIGNED, questionText);

                    WebSocketMessageDTO<GameStatus> statusMessage = new WebSocketMessageDTO<>(
                            WebSocketMessageType.GAME_STATUS_CHANGED, gameStatus);

                    sendAliasToEachPlayer(roomCode);

                    sendToRoom(roomCode, questionMessage);

                    sendToRoom(roomCode, statusMessage);
                    break;

                case CHAT_MESSAGE:

                    String text = (String) webSocketMessageDTO.data();

                    ChatMessageRespDTO chatMessageRespDTO = chatService.sendChatMessage(roomCode, playerId, text);

                    WebSocketMessageDTO<ChatMessageRespDTO> chatMessage = new WebSocketMessageDTO<>(
                            WebSocketMessageType.CHAT_MESSAGE, chatMessageRespDTO);

                    sendToRoom(roomCode, chatMessage);

                    Optional<ChatMessageRespDTO> botReply = botService.generateBotReply(roomCode);

                    if (botReply.isPresent()) {
                        WebSocketMessageDTO<ChatMessageRespDTO> botMessage = new WebSocketMessageDTO<>(
                                WebSocketMessageType.CHAT_MESSAGE, botReply.get());

                        sendToRoom(roomCode, botMessage);

                    }

                    break;

                default:
                    break;
            }

        } catch (Exception e) {
            // Anslutningen stängs inte här om något går fel utan spelaren ska frf vara kvar
            Log.info("Meddelandet kunde inte hanteras: " + e.getMessage());

        }

    }

    private UUID getPlayerId(WebSocketConnection playerConnection) {

        String playerIdText = playerConnection.pathParam("playerId");

        // Källa:
        // https://stackoverflow.com/questions/25637152/easiest-way-in-java-to-turn-string-into-uuid
        UUID playerId = UUID.fromString(playerIdText);

        return playerId;

    }

    // Skickar meddelandet till alla anslutningar där rumskoden är samma som den här
    // spelarens,
    // alltså bara till spelarna i samma rum.
    private <T> void sendToRoom(String roomCode, WebSocketMessageDTO<T> message) {
        connection.broadcast().filter(c -> c.pathParam("roomCode")
                .equalsIgnoreCase(roomCode))
                .sendTextAndAwait(message);
    }

    // Skickar ett alias till varje spelare i rummet på sin egen anslutning.
    // inte broadcast, eftersom alla ska få olika alias och ingen ska se någon
    // annans.
    private void sendAliasToEachPlayer(String roomCode) {

        for (WebSocketConnection playerConnection : connection.getOpenConnections()) {

            if (!playerConnection.pathParam("roomCode").equalsIgnoreCase(roomCode)) {
                continue;
            }

            try {

                String alias = gameRoomService.getAlias(getPlayerId(playerConnection));

                WebSocketMessageDTO<String> message = new WebSocketMessageDTO<>(WebSocketMessageType.ALIAS_ASSIGNED,
                        alias);

                playerConnection.sendTextAndAwait(message);

            } catch (Exception e) {

                Log.info("Kunde inte skicka alias: " + e.getMessage());

            }

        }

    }

}
