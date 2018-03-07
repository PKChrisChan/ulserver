package io.baschel.ulserver.game.handler;

import io.baschel.ulserver.game.GameState;
import io.baschel.ulserver.game.state.GamePlayerRecord;
import io.baschel.ulserver.msgs.internal.SendMessageToRoom;
import io.baschel.ulserver.msgs.lyra.LyraMessage;
import io.baschel.ulserver.msgs.lyra.RMsg_Speech;
import io.baschel.ulserver.msgs.lyra.consts.LyraConsts;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

import static io.baschel.ulserver.msgs.lyra.consts.LyraConsts.Speech.*;

public class SpeechHandler extends GameMessageHandler {

    private static final Logger L = LoggerFactory.getLogger(SpeechHandler.class);

    public SpeechHandler(GameState gs) {
        super(gs);
    }

    @Override
    public void handle(String source, LyraMessage message) {
        GamePlayerRecord gpr = gs.playerRecordForConnectionId(source);
        // Note: we forward directly to the room verticle because it always has the most up-to-date player position.
        if(message instanceof RMsg_Speech) {
            boolean send = true;
            switch(((RMsg_Speech) message).speech_type.charAt(0))
            {
                case REPORT_BUG:
                    logUnsent("BUG", gpr, ((RMsg_Speech) message).speech_text);
                    send = false;
                    break;
                case REPORT_CHEAT:
                    logUnsent("CHEAT", gpr, ((RMsg_Speech) message).speech_text);
                    send = false;
                    break;
                case REPORT_DEBUG:
                    logUnsent("DEBUG", gpr, ((RMsg_Speech) message).speech_text);
                    send = false;
                    break;
                case RP:
                    logUnsent("RP", gpr, ((RMsg_Speech) message).speech_text);
                    send = false;
                    break;
                case RAW_EMOTE:
                case GLOBALSHOUT:
                    if(gpr.acctType != LyraConsts.AcctType.ACCT_ADMIN.toValue()) {
                        send = false;
                        L.warn("Player {} sending illegal raw emote (not an admin!", gpr.pid);
                    }
                    break;
                case AUTO_CHEAT:
                    if(gpr.acctType != LyraConsts.AcctType.ACCT_MONSTER.toValue()) {
                        send = false;
                        logUnsent("AUTOCHEAT", gpr, ((RMsg_Speech) message).speech_text);
                    }
                    break;
                case SERVER_TEXT:
                case SYSTEM_SPEECH:
                    // TODO MDA: we'll probably want to make this sendable from the web API so remove this then.
                    send = false;
                    logUnsent("SERVERTEXT / SYSTEMSPEECH", gpr, ((RMsg_Speech) message).speech_text);
                    break;

            }
            // TODO MDA: send message to player party!
            if(send)
                new SendMessageToRoom(gpr.level, gpr.room).setSender(gpr.pid).setSendToSender(false).setMeesage((RMsg_Speech) message).send();
        }
    }

    private void logUnsent(String type, GamePlayerRecord gpr, String speech_text) {
        L.warn("{} (pid={}, name={}, level={}, room={}, x={}, y={}) {}", type, gpr.pid, gpr.upperName(), gpr.level, gpr.room, gpr.lastUpdate.x,
                gpr.lastUpdate.y, speech_text);
    }

    @Override
    public Set<Class<? extends LyraMessage>> handles() {
        return new HashSet<Class<? extends LyraMessage>>() {{
            add(RMsg_Speech.class);
        }};
    }
}
