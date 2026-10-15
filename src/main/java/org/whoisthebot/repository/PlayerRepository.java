package org.whoisthebot.repository;

import java.util.UUID;

import org.whoisthebot.entity.Player;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped 
public class PlayerRepository implements PanacheRepositoryBase<Player, UUID>{

}
