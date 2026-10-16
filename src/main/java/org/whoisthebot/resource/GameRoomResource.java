package org.whoisthebot.resource;

import org.whoisthebot.DTOs.CreateRoomReqDTO;
import org.whoisthebot.DTOs.JoinRoomReqDTO;
import org.whoisthebot.DTOs.RoomEntryRespDTO;
import org.whoisthebot.entity.Player;
import org.whoisthebot.service.GameRoomService;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


@Path ("/api/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GameRoomResource {

    @Inject GameRoomService gameRoomService;

    @POST 
    @Path("/create")
    public Response createGameRoom(CreateRoomReqDTO createRoomReqDTO) {

        Player player = gameRoomService.createGameRoom(createRoomReqDTO.username());

        RoomEntryRespDTO roomEntryRespDTO = new RoomEntryRespDTO(player);

        return Response.status(Response.Status.CREATED).entity(roomEntryRespDTO).build();

    }

    @POST 
    @Path ("/join")
    public Response joinGameRoom(JoinRoomReqDTO joinRoomReqDTO) {

        Player player = gameRoomService.joinGameRoom(joinRoomReqDTO.username(), joinRoomReqDTO.roomCode());

        RoomEntryRespDTO roomEntryRespDTO = new RoomEntryRespDTO(player);

        return Response.status(Response.Status.CREATED).entity(roomEntryRespDTO).build();
    }

    
}
