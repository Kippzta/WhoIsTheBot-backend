package org.whoisthebot.entity;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Player {

    // usernamet som alltid kommer användas till AI:n
    public static final String BOT_USERNAME = "Imposter";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String username;

    // Sätts när spelet startar så ingen vet vem som är vem
    private String alias = null;

    private boolean isHost = false;

    // Skickas aldrig till frontend under spelets gång för då kan man fuska och se vem som är boten
    private boolean isBot = false;

    // Skapar kopplingen till gameRoom, game_room_id 
    @ManyToOne
    private GameRoom gameRoom;

    public Player(String username, GameRoom gameRoom) {

        this.username = username;

        this.gameRoom = gameRoom;
    }
    
     // Används när en spelare hämtas från databasen
    protected Player() {
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public boolean isHost() {
        return isHost;
    }

    public void setHost(boolean isHost) {
        this.isHost = isHost;
    }

    public boolean isBot() {
        return isBot;
    }

    public void setBot(boolean isBot) {
        this.isBot = isBot;
    }

    public GameRoom getGameRoom() {
        return gameRoom;
    }
}
