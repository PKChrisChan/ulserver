package io.baschel.ulserver.game.handler;

import io.baschel.ulserver.game.GameState;
import io.baschel.ulserver.game.MessageHandler;

public abstract class GameMessageHandler implements MessageHandler {
    protected GameState gs;

    public GameMessageHandler(GameState gs) {
        this.gs = gs;
    }
}
