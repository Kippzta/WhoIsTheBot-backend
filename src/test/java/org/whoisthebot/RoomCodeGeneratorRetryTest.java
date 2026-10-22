package org.whoisthebot;

import static org.mockito.Mockito.when;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.whoisthebot.entity.GameRoom;
import org.whoisthebot.generator.RoomCodeGenerator;
import org.whoisthebot.repository.GameRoomRepository;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
public class RoomCodeGeneratorRetryTest {

    @Inject
    RoomCodeGenerator roomCodeGenerator;

    @InjectMock
    GameRoomRepository gameRoomRepository;

    @Test
    void generatesNewCodeWhenFirstCodeIsTaken() {
        // Arrange
        // anyString säger att vi inte bryr oss om vilket värde som skickas in, alla
        // rumskoder kommer att stämma.
        // Testet kontrollerar att generatorn gör en ny kod om den första redan finns i
        // databasen.
        when(gameRoomRepository.findByRoomCode(anyString()))
                .thenReturn(Optional.of(new GameRoom("CODE EXISTS")))
                .thenReturn(Optional.empty());

        // Act
        roomCodeGenerator.generateUniqueCode();

        // Assert
        verify(gameRoomRepository, times(2)).findByRoomCode(anyString());
    }

}
