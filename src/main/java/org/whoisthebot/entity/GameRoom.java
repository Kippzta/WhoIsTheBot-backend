package org.whoisthebot.entity;

import java.util.ArrayList;
import java.util.List;

import org.whoisthebot.enums.GameStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class GameRoom {

    @Id
    @GeneratedValue
    private Long id;

    // Koden spelare får för att kunna gå med i ett spel
    private String roomCode;

    // Värdet kommer räknas i sekunder
    private int roundDuration = 90;

    // Blir 1 när spelet startar och ökar vid en lika röstning
    private int currentRound = 0;

    // Sparas som text så ordningen spelar ingen roll
    @Enumerated(EnumType.STRING)
    private GameStatus gameStatus = GameStatus.WAITING;

    // Koppling så man kan komma åt Player objekten
    @OneToMany(mappedBy = "gameRoom")
    private List<Player> players = new ArrayList<>();

    public GameRoom(String roomCode) {
        this.roomCode = roomCode;
    }

    // Används när ett rum hämtas från databasen
    protected GameRoom() {
    }

    public String getRoomCode() {
        return roomCode;
    }

    public int getRoundDuration() {
        return roundDuration;
    }

    public void setRoundDuration(int roundDuration) {
        this.roundDuration = roundDuration;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(int currentRound) {
        this.currentRound = currentRound;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public List<Player> getPlayers() {
        return players;
    }
}
