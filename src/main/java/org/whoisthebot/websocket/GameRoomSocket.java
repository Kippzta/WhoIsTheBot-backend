package org.whoisthebot.websocket;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.whoisthebot.DTOs.ChatMessageRespDTO;
import org.whoisthebot.DTOs.GameResultRespDTO;
import org.whoisthebot.DTOs.GameRoomDTO;
import org.whoisthebot.DTOs.WebSocketMessageDTO;
import org.whoisthebot.enums.GameStatus;
import org.whoisthebot.enums.WebSocketMessageType;
import org.whoisthebot.service.BotService;
import org.whoisthebot.service.ChatService;
import org.whoisthebot.service.GameRoomService;
import org.whoisthebot.service.VoteService;

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

    @Inject
    VoteService voteService;

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

            GameStatus gameStatus = gameRoomDTO.gameStatus();

            if (gameStatus != GameStatus.WAITING) {
                String alias = gameRoomService.getAlias(playerId);

                WebSocketMessageDTO<String> aliasUpdateMessage = new WebSocketMessageDTO<>(
                        WebSocketMessageType.ALIAS_ASSIGNED, alias);
                sendToPlayer(aliasUpdateMessage);

                String questionText = gameRoomService.getQuestionText(roomCode);

                WebSocketMessageDTO<String> questionUpdateMessage = new WebSocketMessageDTO<>(
                        WebSocketMessageType.QUESTION_ASSIGNED, questionText);

                sendToPlayer(questionUpdateMessage);
            }

            if (gameStatus == GameStatus.CHATTING) {

                Long secondsLeft = gameRoomService.getChatRoundSecondsLeft(roomCode);

                WebSocketMessageDTO<Long> timerUpdateMessage = new WebSocketMessageDTO<>(
                        WebSocketMessageType.TIMER_STARTED, secondsLeft);

                sendToPlayer(timerUpdateMessage);
            }

            if (gameStatus == GameStatus.VOTING) {
                
                List<String> allAliases = gameRoomService.getShuffledAliases(roomCode);

                WebSocketMessageDTO<List<String>> votingUpdateMessage = new WebSocketMessageDTO<>(
                    WebSocketMessageType.VOTING_STARTED, allAliases);
                sendToPlayer(votingUpdateMessage);

            }

            if (gameStatus == GameStatus.FINISHED) {

                GameResultRespDTO gameResultRespDTO = voteService.getResult(roomCode);
                WebSocketMessageDTO<GameResultRespDTO> resultUpdateMessage = new WebSocketMessageDTO<>(
                    WebSocketMessageType.GAME_RESULT, gameResultRespDTO
                );

                sendToPlayer(resultUpdateMessage);

            }

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
                // Hosten i frontend skickar START_GAME
                case START_GAME:

                    // Startar spelet och returnerar den nya statusen på spelet
                    GameStatus gameStatus = gameRoomService.startGame(roomCode, playerId);

                    // Hämtar frågan som ska ställas i chatten
                    String questionText = gameRoomService.getQuestionText(roomCode);

                    // Hämtar hur lång tid som ska chatt rundan ska vara
                    Long timeleft = gameRoomService.getChatRoundSecondsLeft(roomCode);

                    WebSocketMessageDTO<String> questionMessage = new WebSocketMessageDTO<>(
                            WebSocketMessageType.QUESTION_ASSIGNED, questionText);

                    WebSocketMessageDTO<GameStatus> statusMessage = new WebSocketMessageDTO<>(
                            WebSocketMessageType.GAME_STATUS_CHANGED, gameStatus);

                    WebSocketMessageDTO<Long> timeMessage = new WebSocketMessageDTO<>(
                            WebSocketMessageType.TIMER_STARTED, timeleft);

                    sendAliasToEachPlayer(roomCode);

                    sendToRoom(roomCode, questionMessage);

                    sendToRoom(roomCode, timeMessage);

                    // statusen skickas sist för att vi byter vi i frontend när denna tas emot
                    // då har vyn redan allt som behövs till chatten: alias, frågan och timern
                    sendToRoom(roomCode, statusMessage);
                    break;

                // Tar emot chat meddelanden från frontend och skickar dem till alla spelare i
                // rummet med tillhörande alias om vem som skrev,
                // och genererar ett svar från botten om det finns.
                case CHAT_MESSAGE:

                    // Hämtar meddelandet som skickats och castar det till en String
                    String text = (String) webSocketMessageDTO.data();

                    // Skickar meddelandet till chatService som sparar det i databasen och
                    // returnerar ett objekt med alias och text som ska skickas till alla spelare i
                    // rummet.
                    ChatMessageRespDTO chatMessageRespDTO = chatService.sendChatMessage(roomCode, playerId, text);

                    WebSocketMessageDTO<ChatMessageRespDTO> chatMessage = new WebSocketMessageDTO<>(
                            WebSocketMessageType.CHAT_MESSAGE, chatMessageRespDTO);

                    sendToRoom(roomCode, chatMessage);

                    // Genererar ett svar från botten om det finns och skickar det till alla spelare
                    // i rummet
                    Optional<ChatMessageRespDTO> botReply = botService.generateBotReply(roomCode);

                    if (botReply.isPresent()) {
                        WebSocketMessageDTO<ChatMessageRespDTO> botMessage = new WebSocketMessageDTO<>(
                                WebSocketMessageType.CHAT_MESSAGE, botReply.get());

                        sendToRoom(roomCode, botMessage);

                    }
                    break;

                // Frontend skickar detta när timern når 0.
                case CHAT_TIME_END:

                    // Avslutar chatten och returnerar en lista med allas alias som ska skickas till
                    // frontend för att starta röstningen
                    // eller en tom optional
                    Optional<List<String>> allAliases = gameRoomService.endChat(roomCode);

                    // Om det finns något i optionalen hämta och skicka med det i datan
                    if (allAliases.isPresent()) {

                        WebSocketMessageDTO<List<String>> aliasesMessage = new WebSocketMessageDTO<>(
                                WebSocketMessageType.VOTING_STARTED, allAliases.get());

                        WebSocketMessageDTO<GameStatus> statusMessageVoting = new WebSocketMessageDTO<>(
                                WebSocketMessageType.GAME_STATUS_CHANGED, GameStatus.VOTING);

                        sendToRoom(roomCode, aliasesMessage);

                        sendToRoom(roomCode, statusMessageVoting);

                    }
                    break;

                // En spelare röstar på ett alias. vem som röstat hämtas genom playerId i URLn
                // och skickas med till metoden addVote.
                // När alla människor har röstat avslutas spelet och resultatet skickas till
                // alla.
                case VOTE:

                    String votedForAlias = (String) webSocketMessageDTO.data();

                    boolean allVoted = voteService.addVote(roomCode, playerId, votedForAlias);

                    if (allVoted) {

                        GameResultRespDTO gameResultRespDTO = voteService.getResult(roomCode);

                        WebSocketMessageDTO<GameResultRespDTO> gameResultMessage = new WebSocketMessageDTO<>(
                                WebSocketMessageType.GAME_RESULT, gameResultRespDTO);

                        sendToRoom(roomCode, gameResultMessage);

                        // Avsluta seplet
                        WebSocketMessageDTO<GameStatus> gameFinishedMessage = new WebSocketMessageDTO<>(
                                WebSocketMessageType.GAME_STATUS_CHANGED, GameStatus.FINISHED);

                        sendToRoom(roomCode, gameFinishedMessage);
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

    // Skickar meddelandet till bara den spelaren som är kopplad till anslutningen
    private <T> void sendToPlayer(WebSocketMessageDTO<T> message) {
        connection.sendTextAndAwait(message);
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
