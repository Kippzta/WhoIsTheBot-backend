package org.whoisthebot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.whoisthebot.generator.RoomCodeGenerator;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
public class RoomCodeGeneratorTest {

    private static final int NUMBER_OF_CODES = 1000;

    @Inject
    RoomCodeGenerator roomCodeGenerator;

    // TEST för att kontrollera att koden bara får 5 tecken
    @Test
    void generatedCodeHasFiveCharacters() {

        String code = roomCodeGenerator.generateUniqueCode();

        assertEquals(5, code.length());
    }

    // TEST för att se så karaktärer jag valt att utesluta inte kommer med när koder
    // genereras
    @Test
    void generatedCodeHasNoConfusingCharacters() {

        for (int i = 0; i < NUMBER_OF_CODES; i++) {
            String code = roomCodeGenerator.generateUniqueCode();

            assertFalse(code.contains("I"));
            assertFalse(code.contains("1"));
            assertFalse(code.contains("O"));
            assertFalse(code.contains("0"));

        }
    }

}
