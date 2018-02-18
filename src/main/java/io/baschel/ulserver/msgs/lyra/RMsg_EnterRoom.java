package io.baschel.ulserver.msgs.lyra;

import java.util.ArrayList;
import java.util.List;

public class RMsg_EnterRoom implements LyraMessage {
    public List<RmRemotePlayer> players;
    public RMsg_EnterRoom() {
        players = new ArrayList<>();
    }
}
