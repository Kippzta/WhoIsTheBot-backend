package org.whoisthebot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.whoisthebot.entity.Player;
import org.whoisthebot.service.GameRoomService;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;

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
}
