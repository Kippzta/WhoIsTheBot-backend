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
        // anyString säger att vi inte bryr oss om vilket värde som skickas in, alla rumskoder kommer att stämma.
        // Första gången vi kollar om koden finns i databasen, så returnerar vi ett GameRoom objekt, vilket betyder att koden redan finns.
        // Andra gången vi kollar om koden finns i databasen, så returnerar vi Optional.empty(), vilket betyder att koden inte finns och är unik.
        // Detta visar att om den första koden som genereras redan finns, så genereras en ny kod och kontrolleras igen.
        when(gameRoomRepository.findByRoomCode(anyString()))
            .thenReturn(Optional.of(new GameRoom("CODE EXISTS")))
            .thenReturn(Optional.empty());

        // Act
        roomCodeGenerator.generateUniqueCode();

        // Assert
        verify(gameRoomRepository, times(2)).findByRoomCode(anyString());
    }

}
