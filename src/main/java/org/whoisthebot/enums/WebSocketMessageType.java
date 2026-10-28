package org.whoisthebot.enums;

public enum WebSocketMessageType {

    // Skickas från frontend
    START_GAME,
    CHAT_TIME_END,
    CHAT_MESSAGE, // åt båda håll
    VOTE,

    // Skickas från backend
    ROOM_UPDATED,
    GAME_STATUS_CHANGED,
    ALIAS_ASSIGNED,
    QUESTION_ASSIGNED,
    TIMER_STARTED,
    VOTING_STARTED,
    GAME_RESULT    

}
