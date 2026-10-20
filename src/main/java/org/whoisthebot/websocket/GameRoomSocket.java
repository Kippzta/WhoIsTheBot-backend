package org.whoisthebot.websocket;

import java.util.UUID;

import org.whoisthebot.DTOs.GameRoomDTO;
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
    public void onOpen(){

        String roomCode = connection.pathParam("roomCode");
        String playerIdText = connection.pathParam("playerId");
        
        try {
            // Källa: https://stackoverflow.com/questions/25637152/easiest-way-in-java-to-turn-string-into-uuid
            UUID playerId = UUID.fromString(playerIdText);

            GameRoomDTO gameRoomDTO = gameRoomService.connectPlayer(roomCode, playerId);

            System.out.println(playerId + " " + "Har anslutit till spelrum: " + roomCode);

        } catch (Exception e) {

            System.out.println("Anlutning misslyckad: " + e.getMessage());
            connection.closeAndAwait();
        }
        
    }

    public @OnClose void onClose() {
        System.out.println("Någon har lämnat rummet!");
    }


    
}
