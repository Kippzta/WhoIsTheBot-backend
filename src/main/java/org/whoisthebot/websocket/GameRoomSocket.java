package org.whoisthebot.websocket;

import io.quarkus.websockets.next.OnClose;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.inject.Inject;

@WebSocket (path = "/ws/rooms/{roomCode}")
public class GameRoomSocket {

    @Inject 
    WebSocketConnection connection;

    @OnOpen
    public void onOpen(){

        String roomCode = connection.pathParam("roomCode");
        System.out.println("Någon har anslutit sig till rummet! " + roomCode);
        
    }

    public @OnClose void onClose() {
        System.out.println("Någon har lämnat rummet!");
    }


    
}
