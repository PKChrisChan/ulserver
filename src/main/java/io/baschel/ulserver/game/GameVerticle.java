package io.baschel.ulserver.game;

import io.baschel.ulserver.Main;
import io.baschel.ulserver.msgs.InternalServerMessage;
import io.baschel.ulserver.msgs.MessageUtils;
import io.baschel.ulserver.msgs.internal.DisconnectClient;
import io.baschel.ulserver.msgs.lyra.LyraMessage;
import io.baschel.ulserver.msgs.lyra.RMsg_Speech;
import io.baschel.ulserver.util.Json;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.Set;

import static io.baschel.ulserver.msgs.lyra.consts.LyraConsts.SERVER_TEXT;

public class GameVerticle extends AbstractVerticle implements GameState {
    public static final String EVENTBUS_ADDRESS = GameVerticle.class.getName();
    public static final String INTERNAL_MESSAGE_ADDRESS = EVENTBUS_ADDRESS + ".internal";
    public static final Logger L = LoggerFactory.getLogger(GameVerticle.class.getName());
    private MessageRouter messageRouter = new MessageRouter(this);
    private PlayerSet playerSet = new PlayerSet();

    @Override
    public void start()
    {
        playerSet.addIndex("pid");
        playerSet.addIndex("upperName");
        playerSet.addIndex("billingId");
        playerSet.addIndex("connectionId");
        vertx.eventBus().consumer(EVENTBUS_ADDRESS, this::handleGameMessage);
        vertx.eventBus().consumer(INTERNAL_MESSAGE_ADDRESS, this::handleInternalMessage);
    }

    private void handleInternalMessage(Message<JsonObject> msg) {
        JsonObject m = msg.body();
        InternalServerMessage ism = Json.objectFromJsonObject(m, InternalServerMessage.class);
        if(ism instanceof DisconnectClient)
        {
            DisconnectClient dc = (DisconnectClient)ism;
            L.debug("Got client disconnect for {}", dc.getClientId());
            Set<PlayerRecord> rec = playerSet.getPlayers("connectionId", dc.getClientId());
            if(rec != null)
                rec.forEach(this::logout);
        }
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
        Set<PlayerRecord> players = playerSet.getPlayers("billingId", acctId);
        return players != null && players.size() > 0;
    }

    @Override
    public int playerIdForConnectionId(String connectionId) {
        return 0;
    }


    @Override
    public PlayerRecord getPlayerRecord(int playerId) {
        Set<PlayerRecord> players = playerSet.getPlayers("pid", playerId);
        if(players != null && players.size() > 0)
            return players.toArray(new PlayerRecord[]{})[0];
        return null;
    }

    @Override
    public void sendToPlayer(LyraMessage message, PlayerRecord record) {
        MessageUtils.sendJsonMessage(record.connectionId, message);
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

    public void sendMotd(PlayerRecord rec)
    {
        RMsg_Speech s = new RMsg_Speech();
        s.speech_type = String.valueOf(SERVER_TEXT);
        s.speech_text = Main.config.serverConfig.motd;
        s.playerid = 0;
        s.speech_len = s.speech_text.length();
        s.babble = false;
        sendToPlayer(s, rec);
    }

    @Override
    public void login(PlayerRecord record) {
        playerSet.addPlayer(record);
        sendMotd(record);
    }

    @Override
    public void logout(PlayerRecord record) {
        playerSet.removePlayer(record);
    }
}
