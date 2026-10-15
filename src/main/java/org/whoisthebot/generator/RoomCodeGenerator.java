package org.whoisthebot.generator;

import java.security.SecureRandom;

import org.whoisthebot.repository.GameRoomRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped 
public class RoomCodeGenerator {

    // I 1, O 0 är borttagna för de kan verka frö lika.
    private static final String ALLOWED_CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    
    private static final int ROOM_CODE_LENGTH = 5;
    
    // Källa: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/security/SecureRandom.html
    private final SecureRandom random = new SecureRandom();
    
    @Inject GameRoomRepository gameRoomRepository;

    // Källa: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/StringBuilder.html
    private String generateCode() {

        // Stringbuilder gör så att det inte skapas en ny sträng för varje varv när en karaktär läggs till utan faktiskt bygger på samma sträng.
        StringBuilder builder = new StringBuilder();
        
        for(int i = 0; i < ROOM_CODE_LENGTH; i++) {
            
            // Slumpar ett index i ALLOWED_CHARACTERS. length() gör att det stämmer även om strängen ändras
            int randNumber = random.nextInt(ALLOWED_CHARACTERS.length());

            // hämtar karaktären som hittades vid indexet
            char codeChar = ALLOWED_CHARACTERS.charAt(randNumber);

            // bygger på den karaktären på strängen tills den blir lika lång som ROOM_CODE_LENGTH
            builder.append(codeChar);

        }
        // returnerar koden
        return builder.toString();
    }



    public String generateUniqueCode() {

        String code = generateCode();

        // Om koden redan finns i databasen, generera en ny tills den är unik
        while (gameRoomRepository.findByRoomCode(code).isPresent()){

            code = generateCode();

        }

        return code;

    }


}
