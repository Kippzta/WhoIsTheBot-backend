package org.whoisthebot.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity 
public class ChatMessage {

    public static final int MAX_TEXT_LENGTH = 100;

    @Id 
    @GeneratedValue 
    private Long id;

    @Column (length = MAX_TEXT_LENGTH)
    private String text;

    private Instant sentAt;

    private int round;

    @ManyToOne 
    private Player player; 

    @ManyToOne 
    private GameRoom gameRoom;

    public ChatMessage() {}

    public ChatMessage(String text, Player player, GameRoom gameRoom) {

        this.text = text;

        this.sentAt = Instant.now();

        this.round = gameRoom.getCurrentRound();

        this.player = player;

        this.gameRoom = gameRoom;

    }

    public String getText() {
        return text;
    }

    public Instant getSentAt() {
        return sentAt;
    }


    public int getRound() {
        return round;
    }


    public Player getPlayer() {
        return player;
    }

    public GameRoom getGameRoom() {
        return gameRoom;
    }

    public Long getId() {
        return id;
    }

    
}
