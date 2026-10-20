package org.whoisthebot.DTOs;

import java.util.UUID;

import org.whoisthebot.entity.Player;

// DTO för att skicka information om en spelare som har gått med i ett rum till frontend, innehåller spelarens id och information om rummet som spelaren har gått med i
public record RoomEntryRespDTO(UUID playerId, GameRoomDTO gameRoomDTO) {

    public RoomEntryRespDTO(Player player){

       // Plockar ut spelarens id och skapar en GameRoomDTO med information om rummet som spelaren har gått med i
        this(player.getId(), new GameRoomDTO(player.getGameRoom()));

    }

} 
 