package org.whoisthebot.service;

import java.util.List;

import org.whoisthebot.entity.GameRoom;
import org.whoisthebot.entity.Player;
import org.whoisthebot.enums.GameStatus;
import org.whoisthebot.generator.RoomCodeGenerator;
import org.whoisthebot.repository.GameRoomRepository;
import org.whoisthebot.repository.PlayerRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class GameRoomService {

    @Inject
    GameRoomRepository gameRoomRepository;

    @Inject
    PlayerRepository playerRepository;

    @Inject
    RoomCodeGenerator roomCodeGenerator;

    // Skapar ett nytt rum och en spelare som är host för rummet, returnerar
    // spelaren.
    @Transactional
    public Player createGameRoom(String username) {

        validateUsername(username);

        String code = roomCodeGenerator.generateUniqueCode();

        GameRoom gameRoom = new GameRoom(code);

        gameRoomRepository.persist(gameRoom);

        Player player = createPlayerAndAddToGameRoom(username, gameRoom, true);

        return player;

    }

    // Låter en spelare gå med i ett spelrum, returnerar spelaren.
    @Transactional
    public Player joinGameRoom(String username, String roomCode) {

        validateUsername(username);

        validateGameRoomCode(roomCode);

        GameRoom gameRoom = gameRoomRepository.findByRoomCode(roomCode.toUpperCase())
                .orElseThrow(() -> new NotFoundException("Inget spelrum hittades"));

        validateCorrectGameStatus(gameRoom, GameStatus.WAITING, "Spelet har redan startat!");

        validateUsernameNotTaken(username, gameRoom.getPlayers());

        Player player = createPlayerAndAddToGameRoom(username, gameRoom, false);

        return player;

    }

    // Helper metod som validerar att användarnamnet inte är tomt eller reserverat
    private void validateUsername(String username) {

        if (username == null || username.isBlank()) {
            throw new BadRequestException("Användarnamnet får inte vara tomt!");
        }

        if (username.equalsIgnoreCase(Player.BOT_USERNAME)) {

            throw new BadRequestException("Användarnamnet är reserverat");
        }

    }

    // Helper metod
    // Skapar en spelare och lägger till den i ett rum, returnerar spelaren
    private Player createPlayerAndAddToGameRoom(String username, GameRoom gameRoom, boolean isHost) {

        Player player = new Player(username, gameRoom);

        // Sätter om spelaren är host eller inte
        player.setHost(isHost);

        playerRepository.persist(player);

        // Player äger relationen i databasen, men rummets lista i minnet uppdateras
        // inte av sig själv.
        // Utan detta förblir listan tom.
        gameRoom.addPlayer(player);

        return player;
    }

    // helper metod för att kontrollera att spelrums koden inte är null eller tom.
    private void validateGameRoomCode(String roomCode) {
        if (roomCode == null || roomCode.isBlank()) {
            throw new BadRequestException("Koden får inte vara tom!");
        }
    }

    //helpter metod som kontrollerar att användarnamnet inte redan är taget i rummet
    private void validateUsernameNotTaken(String username, List<Player> players) {

        for (Player player : players) {

            if (player.getUsername().equalsIgnoreCase(username)) {

                throw new BadRequestException("Användarnamnet är upptaget!");

            }

        }
    }


    // helper metod som kontrollerar att spelets status är den som krävs för att just den delen av spelet ska kunna köras
    private void validateCorrectGameStatus(GameRoom gameRoom, GameStatus reqGameStatus, String errorMsg) {

        if (gameRoom.getGameStatus() != reqGameStatus) {

            throw new BadRequestException(errorMsg);

        }

    }

}
