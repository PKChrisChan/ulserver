package io.baschel.ulserver.msgs.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.baschel.ulserver.db.DbVerticle;
import io.baschel.ulserver.msgs.InternalServerMessage;

public class PlayerStatsRequest implements InternalServerMessage {
    public int pid;

    @JsonIgnore
    public boolean failed = false;

    public static PlayerStatsRequest of(int playerId) {
        PlayerStatsRequest r = new PlayerStatsRequest();
        r.pid = playerId;
        return r;
    }

    public int playerId() {
        return pid;
    }

    @Override
    public String address() {
        return DbVerticle.EVENTBUS_ADDRESS;
    }
}
