package io.baschel.ulserver.game;

import io.baschel.ulserver.msgs.lyra.LyraMessage;

import java.util.Set;

public interface MessageHandler {
    void handle(String source, LyraMessage message);
    Set<Class<? extends LyraMessage>> handles();
}
