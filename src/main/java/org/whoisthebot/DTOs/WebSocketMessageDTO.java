package org.whoisthebot.DTOs;

import org.whoisthebot.enums.WebSocketMessageType;
// https://stackoverflow.com/questions/21972248/how-to-define-a-field-to-have-multiple-types
// https://docs.oracle.com/javase/tutorial/java/generics/types.html
public record WebSocketMessageDTO<T>(WebSocketMessageType type, T data) {

} 