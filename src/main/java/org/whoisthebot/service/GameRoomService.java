package org.whoisthebot.service;

import org.whoisthebot.entity.GameRoom;
import org.whoisthebot.entity.Player;
import org.whoisthebot.generator.RoomCodeGenerator;
import org.whoisthebot.repository.GameRoomRepository;
import org.whoisthebot.repository.PlayerRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;

@ApplicationScoped
public class GameRoomService {

    @Inject
    GameRoomRepository gameRoomRepository;

    @Inject
    PlayerRepository playerRepository;

    @Inject
    RoomCodeGenerator roomCodeGenerator;


    // Skapar ett nytt rum och en spelare som är host för rummet, returnerar spelaren.
    public Player createGameRoom(String username) {

        if (username == null || username.isBlank()) {
            throw new BadRequestException("Användarnamnet får inte vara tomt!");
        }

        if (username.equalsIgnoreCase(Player.BOT_USERNAME)) {

            throw new BadRequestException("Användarnamnet är reserverat");
        }

        String code = roomCodeGenerator.generateUniqueCode();

        GameRoom gameRoom = new GameRoom(code);

        gameRoomRepository.persist(gameRoom);

        Player player = new Player(username, gameRoom);

        player.setHost(true);

        playerRepository.persist(player);

        // Player äger relationen i databasen, men rummets lista i minnet uppdateras
        // inte av sig själv.
        // Utan det här visas listan som tom.
        gameRoom.addPlayer(player);

        return player;

    }

}
