package org.whoisthebot.repository;

import java.util.Optional;

import org.whoisthebot.entity.GameRoom;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped 
public class GameRoomRepository implements PanacheRepository<GameRoom>{

    public Optional<GameRoom> findByRoomCode(String roomCode) {
        
        return find("roomCode", roomCode).firstResultOptional();
    
    }

    

}
