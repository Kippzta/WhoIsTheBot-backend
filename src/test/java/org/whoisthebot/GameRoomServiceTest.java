package org.whoisthebot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.whoisthebot.entity.GameRoom;
import org.whoisthebot.entity.Player;
import org.whoisthebot.service.GameRoomService;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

@QuarkusTest
public class GameRoomServiceTest {

    @Inject
    GameRoomService gameRoomService;

    // Test för att se om spelaren som skapade game rummet blir host
    @Test
    void playerWhoCreatesGameBecomesHost() {

        // act
        Player player = gameRoomService.createGameRoom("Erik");

        // assert
        assertTrue(player.isHost());

    }

    // TEST för att kontrollera att rumemt har en kod och att en spelare finns i
    // rummets spelarlista
    @Test
    void createdRoomHasCodeAndContainsCreator() {

        // act
        Player player = gameRoomService.createGameRoom("Erik");

        // assert
        assertNotNull(player.getGameRoom().getRoomCode());

        assertEquals(1, player.getGameRoom().getPlayers().size());

    }

    // TEST om ett användarnamn är null eller tomt, samt mellanslag. Kasta fel.
    @Test
    void throwsExceptionIfUsernameIsNullOrEmpty() {

        assertThrows(BadRequestException.class, () -> gameRoomService.createGameRoom("   "));

        assertThrows(BadRequestException.class, () -> gameRoomService.createGameRoom(null));

    }

    //TEST om en användare väljer samma namn som det reserverade nemnet till Ai:n, kasta fel.
    @Test 
    void throwsExceptionIfUsernameEqualsBotUsername() {

        assertThrows(BadRequestException.class, () -> gameRoomService.createGameRoom("IMPOSTER"));

    }

    // Test för att kontrollera så en spelare kommer in rummet och att listan uppdateras samt att den nya spelaren inte blir host
    @Test 
    void joiningPlayerIsAddedButDoesntBecomeHost() {

        Player host = gameRoomService.createGameRoom("Erik");

        GameRoom gameRoom = host.getGameRoom();

        String roomCode = gameRoom.getRoomCode();

        Player joinedPlayer = gameRoomService.joinGameRoom("Malin", roomCode);

        assertFalse(joinedPlayer.isHost());

        assertEquals(2, joinedPlayer.getGameRoom().getPlayers().size());

    }

    // Test om man försöker joina ett rum som inte finns
    // Valde tecken som inte ingår ALLOWED_CHARACTERS så det kan aldrig bli en kod som skapats
    @Test 
    void throwsExceptionIfGameRoomNotFound() {

         assertThrows(NotFoundException.class, () -> gameRoomService.joinGameRoom("Erik", "GA1A0"));

    }
}
