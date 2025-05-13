package com.rafaxplugins.announce.misc.message.formatter;

import com.rafaxplugins.announce.misc.message.DefaultMessage;
import com.rafaxplugins.announce.misc.message.MessageUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class MessageFormatter<T> {

    protected final String prefix;

    public String getMessage(String message) {
        return MessageUtils.translateColorCodes(this.prefix + message);
    }

    public abstract void send(T sender, String message);

    public void sendDefault(T sender, DefaultMessage defaultMessage, Object... objects) {
        this.send(sender, defaultMessage.format(objects));
    }
}
