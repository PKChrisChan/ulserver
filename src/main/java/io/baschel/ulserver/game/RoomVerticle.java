package io.baschel.ulserver.game;

import io.baschel.ulserver.msgs.InternalServerMessage;
import io.baschel.ulserver.msgs.MessageUtils;
import io.baschel.ulserver.msgs.internal.PlayerEnterRoom;
import io.baschel.ulserver.msgs.internal.PlayerLeaveRoom;
import io.baschel.ulserver.msgs.lyra.LyraMessage;
import io.baschel.ulserver.msgs.lyra.RMsg_EnterRoom;
import io.baschel.ulserver.msgs.lyra.RMsg_LeaveRoom;
import io.baschel.ulserver.msgs.lyra.RMsg_RoomLoginAck;
import io.baschel.ulserver.msgs.lyra.consts.LyraConsts;
import io.baschel.ulserver.util.Json;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class RoomVerticle extends AbstractVerticle {

    public int level;
    public int room;
    public Set<PlayerRecord> players = new HashSet<>();
    private static final Logger L = LoggerFactory.getLogger(RoomVerticle.class);
    public static String eventBusAddress(int level, int room)
    {
        return RoomVerticle.class.getName() + String.format(".L%02dR%02d", level, room);
    }

    public static String eventBusAddress(int level)
    {
        return RoomVerticle.class.getName() + String.format(".L%02d", level);
    }

    @Override
    public void start()
    {
        JsonObject config = config();
        level = config.getInteger("level");
        room = config.getInteger("room");
        vertx.eventBus().consumer(eventBusAddress(level, room), this::handleRoomMessage);
        vertx.eventBus().consumer(eventBusAddress(level), this::handleLevelMessage);
        L.info("deployed RoomVerticle {}", String.format("L%02dR%02d", level, room));
    }

    private void handleLevelMessage(Message<JsonObject> msg) {

    }

    private void handleRoomMessage(Message<JsonObject> msg) {
        InternalServerMessage ism = Json.objectFromJsonObject(msg.body(), InternalServerMessage.class);
        if(ism instanceof PlayerEnterRoom)
            _handle((PlayerEnterRoom) ism, msg);
        if(ism instanceof PlayerLeaveRoom)
            _handle((PlayerLeaveRoom)ism, msg);
    }

    private void _handle(PlayerLeaveRoom leave, Message<JsonObject>msg)
    {
        PlayerRecord leaver = leave.record;
        players.remove(leaver);
        RMsg_LeaveRoom leaverm = new RMsg_LeaveRoom();
        leaverm.playerid = leaver.pid;
        leaverm.status = 'N'; // NORMAL LOGOUT. // TODO MDA hardcoding normal for now - client never looks at this.
        leaverm.lastx = leaver.lastUpdate.x;
        leaverm.lasty = leaver.lastUpdate.y;
        sendToRoom(leaverm, null);
    }

    private void _handle(PlayerEnterRoom enter, Message<JsonObject> msg) {
        RMsg_EnterRoom myEntry = new RMsg_EnterRoom();
        myEntry.players.add(enter.record.remotePlayer());
        sendToRoom(myEntry, enter.record);
        RMsg_EnterRoom others = new RMsg_EnterRoom();
        for(PlayerRecord p : players)
            others.players.add(p.remotePlayer());
        RMsg_RoomLoginAck rla = new RMsg_RoomLoginAck();
        rla.status = LyraConsts.RoomLogin.LOGIN_OK;
        rla.num_neighbors = players.size();
        players.add(enter.record);
        if(others.players.size() > 0)
            MessageUtils.sendJsonMessage(enter.record.connectionId, others);
        MessageUtils.sendJsonMessage(enter.record.connectionId, rla);
    }

    private void sendToRoom(LyraMessage msg, PlayerRecord recordToIgnore) {
        for(PlayerRecord p : players)
        {
            if(recordToIgnore != null && p.equals(recordToIgnore))
                continue;

            MessageUtils.sendJsonMessage(p.connectionId, msg);
        }
    }
}
