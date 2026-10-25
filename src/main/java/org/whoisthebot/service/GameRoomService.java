package org.whoisthebot.service;

import java.util.List;
import java.util.UUID;

import org.whoisthebot.DTOs.GameRoomDTO;
import org.whoisthebot.entity.GameRoom;
import org.whoisthebot.entity.Player;
import org.whoisthebot.enums.GameStatus;
import org.whoisthebot.generator.AliasGenerator;
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

    private static final int MIN_REQ_PLAYERS = 2;

    @Inject
    GameRoomRepository gameRoomRepository;

    @Inject
    PlayerRepository playerRepository;

    @Inject
    RoomCodeGenerator roomCodeGenerator;

    @Inject
    AliasGenerator aliasGenerator;

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

        // Kontrollerar att användarnamnet inte är tomt eller reserverat
        validateUsername(username);

        // Kontrollerar att rumskoden inte är tom eller null
        validateGameRoomCode(roomCode);

        GameRoom gameRoom = gameRoomRepository.findByRoomCode(roomCode.toUpperCase())
                .orElseThrow(() -> new NotFoundException("Inget spelrum hittades"));

        // Kontrollerar att spelets status är WAITING
        validateCorrectGameStatus(gameRoom, GameStatus.WAITING, "Spelet har redan startat!");

        // Kontrollerar att användarnamnet inte redan är upptaget
        validateUsernameNotTaken(username, gameRoom.getPlayers());

        Player player = createPlayerAndAddToGameRoom(username, gameRoom, false);

        return player;
    }

    // Låter hosten starta spelet, returnerar den nya statusen på spelet.
    @Transactional
    public GameStatus startGame(String roomCode, UUID playerId) {

        Player hostPlayer = getPlayerFromCorrectRoom(roomCode, playerId);

        GameRoom gameRoom = hostPlayer.getGameRoom();

        // Kontrollerar att spelaren är host för rummet för bara den spelaren kan starta
        // spelet
        if (!hostPlayer.isHost()) {
            throw new BadRequestException("Spelare måste vara värd för att starta spelet!");
        }

        validateCorrectGameStatus(gameRoom, GameStatus.WAITING, "Spelet har redan startat!");

        // Kontrollerar att det finns minst två spelare i rummet innan spelet kan starta
        if (gameRoom.getPlayers().size() < MIN_REQ_PLAYERS) {
            throw new BadRequestException("Minst två spelare behövs för att starta spelet!");
        }

        // Lägger till botten efter vi kontrollerat spelarlistan för annars skulle den
        // räknas in i kontrollen för att kunna starta spelet.
        Player bot = createPlayerAndAddToGameRoom(Player.BOT_USERNAME, gameRoom, false);

        bot.setBot(true);

        // Genererar alias för alla spelare i rummet, inklusive botten
        List<String> aliases = aliasGenerator.generateAliases(gameRoom.getPlayers().size());

        List<Player> players = gameRoom.getPlayers();

        // Sätter alias för alla spelare i rummet, inklusive botten
        for (int i = 0; i < players.size(); i++) {

            Player currentPlayer = players.get(i);

            String alias = aliases.get(i);

            currentPlayer.setAlias(alias);
        }

        gameRoom.setGameStatus(GameStatus.CHATTING);

        gameRoom.setCurrentRound(1);

        return gameRoom.getGameStatus();

    }

    // kontrollerar att spelare finns och hör till rätt spelrum
    // bygger DTO:n inuti metoden, eftersom spelarlistan hämtas lat (lazy) och inte
    // går att hämta efteråt
    // Källa; https://www.baeldung.com/hibernate-lazy-eager-loading
    @Transactional
    public GameRoomDTO validatePlayerAndGetRoom(String roomCode, UUID playerId) {

        Player player = getPlayerFromCorrectRoom(roomCode, playerId);

        GameRoomDTO gameRoomDTO = new GameRoomDTO(player.getGameRoom());

        return gameRoomDTO;
    }

    // Transactional för att kunna hämta aliaset från databasen, eftersom det är lazy loaded
    @Transactional 
    public String getAlias(UUID playerId) {

        Player player = playerRepository.findByIdOptional(playerId)
                .orElseThrow(() -> new NotFoundException("Hittade ingen spelare med detta id"));

               return player.getAlias();
    }

    //////////////////////////////////////////
    /// **Helpermetoder** ///////////////////
    /////////////////////////////////////////

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

    // helpter metod som kontrollerar att användarnamnet inte redan är taget i
    // rummet
    private void validateUsernameNotTaken(String username, List<Player> players) {

        for (Player player : players) {

            if (player.getUsername().equalsIgnoreCase(username)) {

                throw new BadRequestException("Användarnamnet är upptaget!");

            }

        }
    }

    // helper metod som kontrollerar att spelets status är den som krävs för att
    // just den delen av spelet ska kunna köras
    private void validateCorrectGameStatus(GameRoom gameRoom, GameStatus reqGameStatus, String errorMsg) {

        if (gameRoom.getGameStatus() != reqGameStatus) {

            throw new BadRequestException(errorMsg);

        }

    }

    private Player getPlayerFromCorrectRoom(String roomCode, UUID playerId) {

        Player player = playerRepository.findByIdOptional(playerId)
                .orElseThrow(() -> new NotFoundException("Ingen spelare med det id:t hittades!"));

        if (!roomCode.equalsIgnoreCase(player.getGameRoom().getRoomCode())) {
            throw new BadRequestException("Spelaren tillhör inte det här spelrummet!");
        }

        return player;

    }

}
