package io.baschel.ulserver.game;

import io.baschel.ulserver.Main;
import io.baschel.ulserver.game.state.AbstractPlayerRecord;
import io.baschel.ulserver.game.state.GamePlayerRecord;
import io.baschel.ulserver.game.state.PlayerSet;
import io.baschel.ulserver.msgs.InternalServerMessage;
import io.baschel.ulserver.msgs.MessageUtils;
import io.baschel.ulserver.msgs.internal.DisconnectClient;
import io.baschel.ulserver.msgs.internal.LoggedInPlayersRequest;
import io.baschel.ulserver.msgs.internal.PlayerEnterRoom;
import io.baschel.ulserver.msgs.internal.PlayerLeaveRoom;
import io.baschel.ulserver.msgs.lyra.LyraMessage;
import io.baschel.ulserver.msgs.lyra.RMsg_Speech;
import io.baschel.ulserver.util.Json;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.Set;
import java.util.stream.Collectors;

import static io.baschel.ulserver.msgs.lyra.consts.LyraConsts.Speech.SERVER_TEXT;

public class GameVerticle extends AbstractServerVerticle implements GameState {
    public static final String EVENTBUS_ADDRESS = GameVerticle.class.getName();
    public static final String INTERNAL_MESSAGE_ADDRESS = EVENTBUS_ADDRESS + ".internal";
    public static final Logger L = LoggerFactory.getLogger(GameVerticle.class.getName());
    private MessageRouter messageRouter = new MessageRouter(this);
    private PlayerSet playerSet = new PlayerSet();

    @Override
    public void start() {
        super.start();
        playerSet.addIndex("pid");
        playerSet.addIndex("upperName");
        playerSet.addIndex("billingId");
        playerSet.addIndex("connectionId");
        playerSet.addIndex("locationId");
        playerSet.addIndex("levelId");
        vertx.eventBus().consumer(EVENTBUS_ADDRESS, this::handleGameMessage);
        vertx.eventBus().consumer(INTERNAL_MESSAGE_ADDRESS, this::handleInternalMessage);
    }

    @Override
    public void handleServerGlobalMessage(InternalServerMessage ism) {
        if (ism instanceof DisconnectClient) {
            DisconnectClient dc = (DisconnectClient) ism;
            L.debug("Got client disconnect for {}", dc.getClientId());
            Set<AbstractPlayerRecord> rec = playerSet.getPlayers("connectionId", dc.getClientId());
            if (rec != null)
                rec.forEach(this::logout);
        }
    }

    private void handleInternalMessage(Message<JsonObject> msg) {
        JsonObject m = msg.body();
        InternalServerMessage ism = Json.objectFromJsonObject(m, InternalServerMessage.class);
        if (ism instanceof LoggedInPlayersRequest) {
            msg.reply(new JsonArray(playerSet.getAllPlayers().stream().map(p -> Json.objectToJsonObject(p)).collect(Collectors.toList())));
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
        Set<AbstractPlayerRecord> players = playerSet.getPlayers("billingId", acctId);
        return players != null && players.size() > 0;
    }

    @Override
    public GamePlayerRecord playerRecordForConnectionId(String connectionId) {
        return getPlayerRecordForField("connectionId", connectionId);
    }

    @Override
    public Set<AbstractPlayerRecord> roomPlayers(String locationId) {
        return playerSet.getPlayers("locationId", locationId);
    }

    private GamePlayerRecord getPlayerRecordForField(String field, Object lookup) {
        Set<AbstractPlayerRecord> players = playerSet.getPlayers(field, lookup);
        if (players != null && players.size() > 0)
            return players.toArray(new GamePlayerRecord[]{})[0];
        return null;
    }

    @Override
    public GamePlayerRecord getPlayerRecord(int playerId) {
        return getPlayerRecordForField("pid", playerId);
    }

    @Override
    public void sendToPlayer(LyraMessage message, AbstractPlayerRecord record) {
        MessageUtils.sendJsonMessage(record.connectionId, message);
    }

    @Override
    public void sendToRoom(LyraMessage message, String levelRoomId, AbstractPlayerRecord record) {
        Set<AbstractPlayerRecord> room = playerSet.getPlayers("locationId", levelRoomId);
        if (room == null && record == null)
            return;

        for (AbstractPlayerRecord target : room)
            if (target.equals(record))
                continue;
            else
                sendToPlayer(message, target);
    }

    @Override
    public void sendToLevel(LyraMessage message, String levelId) {

    }

    @Override
    public void sendToGame(LyraMessage message) {

    }

    @Override
    public void movePlayer(GamePlayerRecord player, int level, int room) {
        String levelId = player.levelId();
        String locationId = player.locationId();
        new PlayerLeaveRoom(player.toRoomPlayerRecord(), player.level, player.room).send();
        player.level = level;
        player.room = room;
        new PlayerEnterRoom(player.toRoomPlayerRecord(), level, room).send();
        playerSet.reindexSingleField("levelId", levelId, player);
        playerSet.reindexSingleField("locationId", locationId, player);
    }

    public void sendMotd(AbstractPlayerRecord rec) {
        RMsg_Speech s = new RMsg_Speech();
        s.speech_type = String.valueOf(SERVER_TEXT);
        s.speech_text = Main.config.serverConfig.motd;
        s.playerid = 0;
        s.speech_len = s.speech_text.length();
        s.babble = false;
        sendToPlayer(s, rec);
    }

    @Override
    public void login(AbstractPlayerRecord record) {
        playerSet.addPlayer(record);
        sendMotd(record);
    }

    @Override
    public void logout(AbstractPlayerRecord record) {
        playerSet.removePlayer(record);

    }
}
