package io.baschel.ulserver.game.handler;

import io.baschel.ulserver.game.GameState;
import io.baschel.ulserver.game.state.GamePlayerRecord;
import io.baschel.ulserver.msgs.MessageUtils;
import io.baschel.ulserver.msgs.lyra.GMsg_GotoLevel;
import io.baschel.ulserver.msgs.lyra.LyraMessage;
import io.baschel.ulserver.msgs.lyra.RMsg_GotoRoom;
import io.baschel.ulserver.msgs.lyra.RMsg_LoginAck;
import io.baschel.ulserver.msgs.lyra.consts.LyraConsts;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class LocationChangeHandler extends GameMessageHandler {
    private static final Logger L = LoggerFactory.getLogger(LocationChangeHandler.class);

    public LocationChangeHandler(GameState gs) {
        super(gs);
    }

    @Override
    public void handle(String source, LyraMessage message) {
        if (message instanceof GMsg_GotoLevel)
            _handle(source, (GMsg_GotoLevel) message);
        else if (message instanceof RMsg_GotoRoom)
            _handle(source, (RMsg_GotoRoom) message);
    }

    private void _handle(String source, RMsg_GotoRoom message) {
        GamePlayerRecord record = gs.playerRecordForConnectionId(source);
        gs.movePlayer(record, record.level, message.roomid);
    }

    private void _handle(String source, GMsg_GotoLevel message) {
        // TODO MDA: Check if player is allowed to move to this room.
        GamePlayerRecord record = gs.playerRecordForConnectionId(source);
        RMsg_LoginAck la = new RMsg_LoginAck();
        if (record == null) {
            L.warn("Connection ID {} not found in playerSet?", source);
            la.status = LyraConsts.RoomLogin.LOGIN_PLAYERNOTFOUND;
            MessageUtils.sendJsonMessage(source, la);
        }

        if (record.level == message.levelid && record.room == message.roomid)
            la.status = LyraConsts.RoomLogin.LOGIN_ALREADYIN;

        record.lastUpdate = message.update;
        la.status = LyraConsts.RoomLogin.LOGIN_OK;
        la.roomid = message.levelid;
        la.levelid = message.roomid;
        gs.sendToPlayer(la, record);
        gs.movePlayer(record, message.levelid, message.roomid);
    }

    @Override
    public Set<Class<? extends LyraMessage>> handles() {
        return new HashSet<Class<? extends LyraMessage>>() {{
            add(GMsg_GotoLevel.class);
            add(RMsg_GotoRoom.class);
        }};
    }
}
