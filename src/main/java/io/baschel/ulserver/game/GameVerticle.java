package io.baschel.ulserver.game;

import io.baschel.ulserver.msgs.lyra.LyraMessage;
import io.baschel.ulserver.util.Json;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class GameVerticle extends AbstractVerticle implements GameState {
    public static final String EVENTBUS_ADDRESS = GameVerticle.class.getName();
    public static final Logger L = LoggerFactory.getLogger(GameVerticle.class.getName());
    private MessageRouter messageRouter = new MessageRouter(this);

    @Override
    public void start()
    {
        vertx.eventBus().consumer(EVENTBUS_ADDRESS, this::handleGameMessage);
    }

    private void handleGameMessage(Message<JsonObject> msg) {
        JsonObject m = msg.body();
        String origin = m.getString("origin");
        JsonObject payload = m.getJsonObject("payload");
        LyraMessage message = Json.objectFromJsonObject(payload, LyraMessage.class);
        handle(origin, message);
    }

    private void handle(String source, LyraMessage message) {
        messageRouter.handle(source, message);
    }

    @Override
    public boolean isAccountLoggedIn(int acctId) {
        return false;
    }

    @Override
    public int playerIdForConnectionId(String connectionId) {
        return 0;
    }


    @Override
    public PlayerRecord getPlayerRecord(int playerId) {
        return null;
    }

    @Override
    public void sendToPlayer(LyraMessage message, int playerId) {

    }

    @Override
    public void sendToRoom(LyraMessage message, String levelRoomId) {

    }

    @Override
    public void sendToLevel(LyraMessage message, String levelId) {

    }

    @Override
    public void sendToGame(LyraMessage message) {

    }
}
