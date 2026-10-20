package org.whoisthebot.websocket;

import java.util.UUID;

import org.whoisthebot.DTOs.GameRoomDTO;
import org.whoisthebot.DTOs.WebSocketMessageDTO;
import org.whoisthebot.enums.WebSocketMessageType;
import org.whoisthebot.service.GameRoomService;

import io.quarkus.websockets.next.OnClose;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.inject.Inject;

@WebSocket(path = "/ws/rooms/{roomCode}/{playerId}")
public class GameRoomSocket {

    @Inject
    GameRoomService gameRoomService;

    @Inject
    WebSocketConnection connection;

    @OnOpen
    public void onOpen() {

        // Hämtar path parametrar från URL:en och sätter dem till variabler
        String roomCode = connection.pathParam("roomCode");
        String playerIdText = connection.pathParam("playerId");

        try {
            // Källa:
            // https://stackoverflow.com/questions/25637152/easiest-way-in-java-to-turn-string-into-uuid
            UUID playerId = UUID.fromString(playerIdText);

            // Validerar spelarens id och rumskoden så rätt spelare finns och tillhör rätt
            // rum, returnerar rummet som en DTO för att
            // kunna skicka det till frontend.
            GameRoomDTO gameRoomDTO = gameRoomService.validatePlayerAndGetRoom(roomCode, playerId);

            System.out.println(playerId + " " + "Har anslutit till spelrum: " + roomCode);

            // Skapar ett meddelande som skickas till alla spelare i rummet för att
            // uppdatera spelarlistan i frontend.
            WebSocketMessageDTO<GameRoomDTO> message = new WebSocketMessageDTO<>(WebSocketMessageType.ROOM_UPDATED,
                    gameRoomDTO);

            // Skickar meddelandet till alla anslutningar där rumskoden är samma som den här
            // spelarens,
            // alltså bara till spelarna i samma rum.
            connection.broadcast().filter(c -> c.pathParam("roomCode")
                    .equalsIgnoreCase(roomCode))
                    .sendTextAndAwait(message);
        } 
          //Fångar om något går fel och stänger anslutningen
        catch (Exception e) {

            System.out.println("Anlutning misslyckad: " + e.getMessage());
            connection.closeAndAwait();
        }

    }

    public @OnClose void onClose() {
        System.out.println("Någon har lämnat rummet!");
    }

}
