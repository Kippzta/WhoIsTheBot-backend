package org.whoisthebot.DTOs;

import java.util.ArrayList;
import java.util.List;

import org.whoisthebot.entity.GameRoom;
import org.whoisthebot.entity.Player;
import org.whoisthebot.enums.GameStatus;

// DTO för att skicka information om ett rum till frontend, innehåller rums-kod, status och en lista med spelarna som är med i rummet
public record GameRoomDTO(String roomCode, GameStatus gameStatus, List<PlayerDTO> players) {

    public GameRoomDTO(GameRoom gameRoom) {

       this(gameRoom.getRoomCode(), gameRoom.getGameStatus(), toPlayerDTOs(gameRoom.getPlayers()));

    }

    private static List<PlayerDTO> toPlayerDTOs(List<Player> players) {

        List<PlayerDTO> playerDTOs = new ArrayList<>();

        for (Player player : players) {

            PlayerDTO playerDTO = new PlayerDTO(player);

            playerDTOs.add(playerDTO);
            
        }

        return playerDTOs;
    }

}