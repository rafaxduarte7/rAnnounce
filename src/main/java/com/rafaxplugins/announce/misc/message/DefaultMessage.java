package com.rafaxplugins.announce.misc.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class DefaultMessage {

    public static final DefaultMessage PLAYER_NOT_FOUND = new DefaultMessage("O jogador %s não foi encontrado.");
    public static final DefaultMessage PLAYER_NOT_ONLINE = new DefaultMessage("O jogador %s não está online.");
    public static final DefaultMessage NO_PERMISSION = new DefaultMessage(
            "Você precisa do grupo %s ou superior para fazer isso.",
            "Você não tem permissão para fazer isso."
    );

    private final String rawMessage;

    @NonNull
    private String defaultRawMessage = "";

    public String format(Object... objects) {
        if (objects.length == 0 && !defaultRawMessage.isEmpty()) {
            return defaultRawMessage;
        }

        return String.format(this.rawMessage, objects);
    }
}
