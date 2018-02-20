package io.baschel.ulserver.game;

import io.baschel.ulserver.msgs.InternalServerMessage;
import io.baschel.ulserver.msgs.MessageUtils;
import io.baschel.ulserver.msgs.internal.DisconnectClient;
import io.baschel.ulserver.msgs.internal.LoggedInPlayersRequest;
import io.baschel.ulserver.msgs.internal.PlayerEnterRoom;
import io.baschel.ulserver.msgs.internal.PlayerLeaveRoom;
import io.baschel.ulserver.msgs.lyra.*;
import io.baschel.ulserver.msgs.lyra.consts.LyraConsts;
import io.baschel.ulserver.util.Json;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RoomVerticle extends AbstractServerVerticle {

    public int level;
    public int room;
    public PlayerSet players;
    private static final Logger L = LoggerFactory.getLogger(RoomVerticle.class);
    Set<PlayerRecord> playersWithPendingUpdates = new HashSet<>();
    public static String eventBusAddress(int level, int room)
    {
        return RoomVerticle.class.getName() + String.format(".L%02dR%02d", level, room);
    }

    public static String eventBusAddressPosUpdate(int level, int room)
    {
        return RoomVerticle.class.getName() + String.format(".L%02dR%02d.posupdates", level, room);
    }


    public static String eventBusAddress(int level)
    {
        return RoomVerticle.class.getName() + String.format(".L%02d", level);
    }

    @Override
    public void start()
    {
        super.start();
        JsonObject config = config();
        level = config.getInteger("level");
        room = config.getInteger("room");
        vertx.eventBus().consumer(eventBusAddress(level, room), this::handleRoomMessage);
        vertx.eventBus().consumer(eventBusAddressPosUpdate(level, room), this::handlePosUpdate);
        vertx.eventBus().consumer(eventBusAddress(level), this::handleLevelMessage);
        vertx.setPeriodic(125, this::onTimer);
        L.info("deployed RoomVerticle {}", String.format("L%02dR%02d", level, room));
        players = new PlayerSet();
        players.addIndex("pid");
        players.addIndex("connectionId");
    }

    @Override
    public void handleServerGlobalMessage(InternalServerMessage ism) {
        if(ism instanceof DisconnectClient)
        {
            players.getPlayers("connectionId", ((DisconnectClient) ism).getClientId()).forEach(r -> {
                leaveRoom(r);
            });
        }
    }

    private void onTimer(Long aLong) {
        if(playersWithPendingUpdates.size() > 0)
        {
            for(PlayerRecord p : players.getAllPlayers())
            {
                List<LmPeerUpdate> upsForPlayer = playersWithPendingUpdates.stream().filter(u -> u.pid != p.pid).map(o->o.lastUpdate)
                        .collect(Collectors.toList());
                if(upsForPlayer.size() > 0) {
                    List<RMsg_PlayerUpdate> updateMessages = RMsg_PlayerUpdate.updatesForList(p.pid, upsForPlayer);
                    updateMessages.forEach(m -> MessageUtils.sendRawMessage(p.connectionId, m, m.MSG_TYPE));
                }
            }
        }
        playersWithPendingUpdates.clear();
    }

    private void handlePosUpdate(Message<Buffer> update) {
        RMsg_Update up = new RMsg_Update().initFromBinary(update.body());
        PlayerRecord rec = players.getPlayers("pid", up.update.playerid).toArray(new PlayerRecord[] {})[0];
        if(!rec.lastUpdate.equals(up.update)) {
            rec.lastUpdate = up.update;
            playersWithPendingUpdates.add(rec);
        }
    }

    private void handleLevelMessage(Message<JsonObject> msg) {
        InternalServerMessage ism = Json.objectFromJsonObject(msg.body(), InternalServerMessage.class);

        if(ism instanceof LoggedInPlayersRequest)
            msg.reply(new JsonArray(players.getAllPlayers().stream().map(Json::objectToJsonObject).collect(Collectors.toList())));
    }

    private void handleRoomMessage(Message<JsonObject> msg) {
        InternalServerMessage ism = Json.objectFromJsonObject(msg.body(), InternalServerMessage.class);
        if(ism instanceof PlayerEnterRoom)
            _handle((PlayerEnterRoom) ism, msg);
        if(ism instanceof PlayerLeaveRoom)
            _handle((PlayerLeaveRoom)ism, msg);
        if(ism instanceof LoggedInPlayersRequest)
            msg.reply(new JsonArray(players.getAllPlayers().stream().map(Json::objectToJsonObject).collect(Collectors.toList())));
    }

    private void _handle(PlayerLeaveRoom leave, Message<JsonObject>msg)
    {
        PlayerRecord leaver = leave.record;
        leaveRoom(leaver);
    }

    private void leaveRoom(PlayerRecord leaver)
    {
        players.removePlayer(leaver);
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
        for(PlayerRecord p : players.getAllPlayers())
            others.players.add(p.remotePlayer());
        RMsg_RoomLoginAck rla = new RMsg_RoomLoginAck();
        rla.status = LyraConsts.RoomLogin.LOGIN_OK;
        rla.num_neighbors = players.getAllPlayers().size();
        players.addPlayer(enter.record);
        if(others.players.size() > 0)
            MessageUtils.sendJsonMessage(enter.record.connectionId, others);
        MessageUtils.sendJsonMessage(enter.record.connectionId, rla);
    }

    private void sendToRoom(LyraMessage msg, PlayerRecord recordToIgnore) {
        for(PlayerRecord p : players.getAllPlayers())
        {
            if(recordToIgnore != null && p.equals(recordToIgnore))
                continue;

            MessageUtils.sendJsonMessage(p.connectionId, msg);
        }
    }
}
