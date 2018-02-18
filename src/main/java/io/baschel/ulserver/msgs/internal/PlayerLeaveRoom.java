package io.baschel.ulserver.msgs.internal;

import io.baschel.ulserver.game.PlayerRecord;
import io.baschel.ulserver.game.RoomVerticle;
import io.baschel.ulserver.msgs.InternalServerMessage;

public class PlayerLeaveRoom implements InternalServerMessage {

    public PlayerRecord record;
    public int room;
    public int level;

    public PlayerLeaveRoom()
    {
        this(null, -1, -1);
    }

    public PlayerLeaveRoom(PlayerRecord rec, int level, int room)
    {
        this.record = rec;
        this.room = room;
        this.level = level;
    }

    @Override
    public String address() {
        if(room > 0 && level > 0)
            return RoomVerticle.eventBusAddress(level, room);
        else if (level > 0)
            return RoomVerticle.eventBusAddress(level);
        else return "";
    }
}
