package io.baschel.ulserver.game.handler;

import io.baschel.ulserver.game.GameState;
import io.baschel.ulserver.msgs.lyra.GMsg_ChangeAvatar;
import io.baschel.ulserver.msgs.lyra.GMsg_ChangeStat;
import io.baschel.ulserver.msgs.lyra.LyraMessage;

import java.util.HashSet;
import java.util.Set;

public class PlayerUpdatesHandler extends GameMessageHandler {
    public PlayerUpdatesHandler(GameState gs)
    {
        super(gs);
    }

    @Override
    public void handle(String source, LyraMessage message) {
        if(message instanceof GMsg_ChangeStat)
            _handle(source, (GMsg_ChangeStat)message);
    }

    private void _handle(String source, GMsg_ChangeStat message) {

    }

    @Override
    public Set<Class<? extends LyraMessage>> handles() {
        return new HashSet<Class<? extends LyraMessage>>() {{
            add(GMsg_ChangeStat.class);
            add(GMsg_ChangeAvatar.class);
        }};
    }
}
