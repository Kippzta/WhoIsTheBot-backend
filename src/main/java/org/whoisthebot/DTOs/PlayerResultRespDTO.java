package org.whoisthebot.DTOs;

import org.whoisthebot.entity.Player;

public record PlayerResultRespDTO(String username, String alias, String votedForAlias, boolean guessedRight) {

    public PlayerResultRespDTO(Player player, String botAlias) {
        this(player.getUsername(), player.getAlias(), player.getVotedForAlias(), botAlias.equals(player.getVotedForAlias()));
    }

}