package io.baschel.ulserver.game;

import io.baschel.ulserver.game.state.AbstractPlayerRecord;
import io.baschel.ulserver.game.state.PlayerSet;
import io.baschel.ulserver.game.state.RoomPlayerRecord;
import io.baschel.ulserver.msgs.InternalServerMessage;
import io.baschel.ulserver.msgs.MessageUtils;
import io.baschel.ulserver.msgs.internal.*;
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

import static io.baschel.ulserver.msgs.lyra.consts.LyraConsts.Speech.*;

public class RoomVerticle extends AbstractServerVerticle {

    public int level;
    public int room;
    public PlayerSet players;
    public static final int SPEECH_DIST = 1300*1300;
    private static final Logger L = LoggerFactory.getLogger(RoomVerticle.class);
    Set<AbstractPlayerRecord> playersWithPendingUpdates = new HashSet<>();

    public static String eventBusAddress(int level, int room) {
        return RoomVerticle.class.getName() + String.format(".L%02dR%02d", level, room);
    }

    public static String eventBusAddressPosUpdate(int level, int room) {
        return RoomVerticle.class.getName() + String.format(".L%02dR%02d.posupdates", level, room);
    }


    public static String eventBusAddress(int level) {
        return RoomVerticle.class.getName() + String.format(".L%02d", level);
    }

    @Override
    public void start() {
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
        if (ism instanceof DisconnectClient) {
            players.getPlayers("connectionId", ((DisconnectClient) ism).getClientId()).forEach(r -> {
                leaveRoom(r);
            });
        }
    }

    private void onTimer(Long aLong) {
        if (playersWithPendingUpdates.size() > 0) {
            for (AbstractPlayerRecord p : players.getAllPlayers()) {
                List<LmPeerUpdate> upsForPlayer = playersWithPendingUpdates.stream().filter(u -> u.pid != p.pid).map(o -> o.lastUpdate)
                        .collect(Collectors.toList());
                if (upsForPlayer.size() > 0) {
                    List<RMsg_PlayerUpdate> updateMessages = RMsg_PlayerUpdate.updatesForList(p.pid, upsForPlayer);
                    updateMessages.forEach(m -> MessageUtils.sendRawMessage(p.connectionId, m, m.MSG_TYPE));
                }
            }
        }
        playersWithPendingUpdates.clear();
    }

    private void handlePosUpdate(Message<Buffer> update) {
        RMsg_Update up = new RMsg_Update().initFromBinary(update.body());
        RoomPlayerRecord rec = (RoomPlayerRecord)players.getPlayer("pid", up.update.playerid);
        if(rec == null)
            L.warn("Received pos update for {} but no record found", up.update.playerid);

        if (!rec.lastUpdate.equals(up.update)) {
            rec.lastUpdate = up.update;
            playersWithPendingUpdates.add(rec);
        }
    }

    private void handleLevelMessage(Message<JsonObject> msg) {
        InternalServerMessage ism = Json.objectFromJsonObject(msg.body(), InternalServerMessage.class);

        if (ism instanceof LoggedInPlayersRequest)
            msg.reply(new JsonArray(players.getAllPlayers().stream().map(Json::objectToJsonObject).collect(Collectors.toList())));
    }

    private void handleRoomMessage(Message<JsonObject> msg) {
        InternalServerMessage ism = Json.objectFromJsonObject(msg.body(), InternalServerMessage.class);
        if (ism instanceof PlayerEnterRoom)
            _handle((PlayerEnterRoom) ism, msg);
        if (ism instanceof PlayerLeaveRoom)
            _handle((PlayerLeaveRoom) ism, msg);
        if (ism instanceof LoggedInPlayersRequest)
            msg.reply(new JsonArray(players.getAllPlayers().stream().map(Json::objectToJsonObject).collect(Collectors.toList())));
        if (ism instanceof SendMessageToRoom) {
            // just do it here.
            boolean send = true;
            SendMessageToRoom m = (SendMessageToRoom) ism;
            if (m.message instanceof RMsg_ChangeAvatar)
                send = send && updatePlayerAvatar(m.sendingPid, ((RMsg_ChangeAvatar) m.message).avatar);
            else if (m.message instanceof RMsg_Speech)
                send = send && handleSpeech(m.sendingPid, (RMsg_Speech) m.message);

            if(send)
                sendToRoom(m.message, m.sendToSourcePid ? null : players.getPlayer("pid", m.sendingPid));
        }

    }

    private boolean handleSpeech(int sourcepid, RMsg_Speech speechMsg)
    {
        AbstractPlayerRecord senderRec = players.getPlayer("pid", sourcepid);
        if(senderRec == null)
        {
            L.warn("Received speech from {} but no record found - ignoring!", sourcepid);
            return false;
        }

        RMsg_Speech outMessage = makeSpeechOutMessage(senderRec, speechMsg);
        switch(speechMsg.speech_type.charAt(0))
        {
            case EMOTE:
            case SPEECH:
            case MONSTER_SPEECH:
            case WHISPER_EMOTE:
                players.getAllPlayers().stream().filter(rec -> Math.pow(rec.lastUpdate.x - senderRec.lastUpdate.x, 2) + Math.pow(rec.lastUpdate.y - senderRec.lastUpdate.y, 2) <= SPEECH_DIST
                        && rec.pid != senderRec.pid).forEach(rec -> MessageUtils.sendJsonMessage(rec.connectionId, outMessage));
                break;
            case SHOUT:
            case RAW_EMOTE:
                sendToRoom(outMessage, senderRec);
                break;
            case GLOBALSHOUT:
                sendToRoom(outMessage, null);
                break;
            case WHISPER:
            case SYSTEM_WHISPER:
                AbstractPlayerRecord target = players.getPlayer("pid", speechMsg.playerid);
                if(target != null)
                    MessageUtils.sendJsonMessage(target.connectionId, outMessage);
                break;
        }

        return false;
    }

    private RMsg_Speech makeSpeechOutMessage(AbstractPlayerRecord fromPlayer, RMsg_Speech speechMsg)
    {
        RMsg_Speech out = new RMsg_Speech();
        out.speech_text = speechMsg.speech_text;
        out.speech_type = speechMsg.speech_type;
        out.playerid = fromPlayer.pid;
        out.babble = speechMsg.babble;
        out.speech_len = speechMsg.speech_len;
        return out;
    }

    private boolean updatePlayerAvatar(int sendingPid, LmAvatar avatar) {
        RoomPlayerRecord rpr = players.getPlayers("pid", sendingPid).toArray(new RoomPlayerRecord[]{})[0];
        rpr.avatar = avatar;
        return true;
    }

    private void _handle(PlayerLeaveRoom leave, Message<JsonObject> msg) {
        AbstractPlayerRecord leaver = leave.record;
        leaveRoom(leaver);
    }

    private void leaveRoom(AbstractPlayerRecord leaver) {
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
        RoomPlayerRecord rpr = (RoomPlayerRecord) enter.record;
        myEntry.players.add(rpr.remotePlayer(room));
        sendToRoom(myEntry, enter.record);
        RMsg_EnterRoom others = new RMsg_EnterRoom();
        for (AbstractPlayerRecord p : players.getAllPlayers())
            others.players.add(((RoomPlayerRecord) p).remotePlayer(room));
        RMsg_RoomLoginAck rla = new RMsg_RoomLoginAck();
        rla.status = LyraConsts.RoomLogin.LOGIN_OK;
        rla.num_neighbors = players.getAllPlayers().size();
        players.addPlayer(enter.record);
        if (others.players.size() > 0)
            MessageUtils.sendJsonMessage(enter.record.connectionId, others);
        MessageUtils.sendJsonMessage(enter.record.connectionId, rla);
    }

    private void sendToRoom(LyraMessage msg, AbstractPlayerRecord recordToIgnore) {
        for (AbstractPlayerRecord p : players.getAllPlayers()) {
            if (recordToIgnore != null && p.equals(recordToIgnore))
                continue;

            MessageUtils.sendJsonMessage(p.connectionId, msg);
        }
    }
}
