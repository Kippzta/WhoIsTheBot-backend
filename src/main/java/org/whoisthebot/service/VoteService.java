package org.whoisthebot.service;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.whoisthebot.DTOs.GameResultRespDTO;
import org.whoisthebot.DTOs.PlayerResultRespDTO;
import org.whoisthebot.entity.GameRoom;
import org.whoisthebot.entity.Player;
import org.whoisthebot.enums.GameStatus;
import org.whoisthebot.repository.GameRoomRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class VoteService {

    @Inject
    GameRoomService gameRoomService;

    @Inject
    GameRoomRepository gameRoomRepository;

    // Metod som lägger till vem spelaren röstat på och returnerar true när alla
    // spelare i rummet har röstat
    @Transactional
    public boolean addVote(String roomCode, UUID playerId, String votedForAlias) {

        Player player = gameRoomService.getPlayerFromCorrectRoom(roomCode, playerId);

        GameRoom gameRoom = player.getGameRoom();

        gameRoomService.validateCorrectGameStatus(gameRoom, GameStatus.VOTING,
                "Det går inte att rösta för spelet är inte klart");

        // OM rösten är null eller blank, kasta fel
        if (votedForAlias == null || votedForAlias.isBlank()) {
            throw new BadRequestException("Inget alias hittades");
        }

        // OM spelaren redan röstat, kasta fel
        if (player.getVotedForAlias() != null) {
            throw new BadRequestException("Du har redan röstat");
        }

        // OM spelaren försöker rösta på sig själv, kasta fel
        if (votedForAlias.equalsIgnoreCase(player.getAlias())) {
            throw new BadRequestException("Du kan inte rösta på dig själv");
        }

        boolean aliasExists = false;

        // Kollar om aliaset som spelaren röstat på finns i rummet
        for (Player roomPlayer : gameRoom.getPlayers()) {

            if (votedForAlias.equals(roomPlayer.getAlias())) {
                aliasExists = true;
                break;
            }
        }

        // OM aliaset inte finns i rummet, kasta fel
        if (!aliasExists) {
            throw new BadRequestException("Det finns ingen spelare med det aliaset");
        }

        // Sätter spelarens röst på det som kom in från frontend
        // Detta sparas automatiskt i databasen eftersom metoden är transactional
        // och player är en entitet som är kopplad till databasen
        player.setVotedForAlias(votedForAlias);

        // Kollar om alla spelare i rummet har röstat, returnerar true om alla har
        // röstat
        for (Player votingPlayer : gameRoom.getPlayers()) {

            // Kollar om spelaren är boten, om den är det så hoppar vi över den
            // annars skulle spelet aldrig sluta eftersom botten inte kan rösta
            if (votingPlayer.isBot()) {
                continue;
            }

            // OM en spelare inte har röstat än så har inte alla röstat och returnerar då false
            if (votingPlayer.getVotedForAlias() == null) {
                return false;
            }
        }

        return true;

    }

    // metod för att hämta resultatet och returnera resultatet
    @Transactional
    public GameResultRespDTO getResult(String roomCode) {

        GameRoom gameRoom = gameRoomRepository.findByRoomCode(roomCode.toUpperCase())
                .orElseThrow(() -> new NotFoundException("inget spelrum hittades"));

        // Sätter statusen på spelet till FINISHED
        gameRoom.setGameStatus(GameStatus.FINISHED);

        String botAlias = null;

        // Loppar för att hitta bottens alias
        for (Player player : gameRoom.getPlayers()) {

            if (player.isBot()) {
                botAlias = player.getAlias();
                break;
            }

        }
        // Listan som ska innehålla alla spelare och deras resultat
        List<PlayerResultRespDTO> playerResults = new ArrayList<>();

        for (Player player : gameRoom.getPlayers()) {

            // Om spealren är botten ska dne inte vara med i resultatet
            if (player.isBot()) {
                continue;
            }

            // Lägger till spelaren i listan med resultatet
            playerResults.add(new PlayerResultRespDTO(player, botAlias));

        }

        return new GameResultRespDTO(botAlias, playerResults);

    }

}
