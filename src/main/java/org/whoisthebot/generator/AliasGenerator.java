package org.whoisthebot.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped 
public class AliasGenerator {
    
   
    private static final List<String> ALIASES = List.of("Gubben", "Liraren", "Slacker", "Surgubben", "Stumpen", "Den Ovetande", "Mulle", "Sladdisen", "Rumpnissen", "Skogsministern");


    // Slumpar ALIASES listan och returnerar en slumpat lista utifrån hur många spelare som behöver ett alias
    public List<String> generateAliases(int playerCount){

        if(playerCount > ALIASES.size()) {
            throw new IllegalArgumentException("Finns inte tillräckligt med alias att dela ut till alla spelare");
        }

        List<String> shuffledAliases = new ArrayList<>(ALIASES);

        Collections.shuffle(shuffledAliases);

       List<String> aliases = shuffledAliases.subList(0, playerCount);
        
       return aliases;
    }

}
