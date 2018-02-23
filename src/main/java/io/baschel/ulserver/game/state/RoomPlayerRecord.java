package io.baschel.ulserver.game.state;

import io.baschel.ulserver.msgs.lyra.RmRemotePlayer;

public class RoomPlayerRecord extends AbstractPlayerRecord {

    public RmRemotePlayer remotePlayer(int room)
    {
        RmRemotePlayer rp = new RmRemotePlayer();
        rp.avatar = avatar;
        rp.playername = name;
        rp.room = room;
        rp.update = lastUpdate;
        return rp;
    }
}
