package io.baschel.ulserver.game.handler;

import io.baschel.ulserver.game.GameState;
import io.baschel.ulserver.game.MessageHandler;
import io.baschel.ulserver.msgs.MessageUtils;
import io.baschel.ulserver.msgs.lyra.GMsg_Ping;
import io.baschel.ulserver.msgs.lyra.LyraMessage;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PingHandler extends GameMessageHandler {
    private static final Logger L = LoggerFactory.getLogger(PingHandler.class);

    public PingHandler(GameState gs) {
        super(gs);
    }

    @Override
    public void handle(String source, LyraMessage message) {
        if(message instanceof GMsg_Ping)
            _handle(source, (GMsg_Ping)message);
    }

    private void _handle(String source, GMsg_Ping message)
    {
        if(message.nonce != 0)
            MessageUtils.sendJsonMessage(source, message);
        else
            L.debug("RCV PING, source cxn: {}", source);
    }

    @Override
    public Set<Class<? extends LyraMessage>> handles() {
        return Collections.unmodifiableSet(new HashSet<Class<? extends LyraMessage>>() {{
            add(GMsg_Ping.class);
        }});
    }
}
