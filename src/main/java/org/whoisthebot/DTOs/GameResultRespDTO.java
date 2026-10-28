package org.whoisthebot.DTOs;

import java.util.List;

public record GameResultRespDTO(String botAlias, List<PlayerResultRespDTO> playerResultRespDTO) {


}