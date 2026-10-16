package org.whoisthebot.DTOs;

import org.whoisthebot.entity.Player;


// DTO för att skicka information om en spelare till frontend, innehåller användarnamn och om spelaren är host eller inte
public record PlayerDTO(String username, boolean isHost) {


    public PlayerDTO(Player player) {
       
       this(player.getUsername(), player.isHost());

    }

} 