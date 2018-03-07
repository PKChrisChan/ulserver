package io.baschel.ulserver.msgs.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.baschel.ulserver.db.DbVerticle;
import io.baschel.ulserver.msgs.InternalServerMessage;

public class PlayerInventoryRequest implements InternalServerMessage {
    public int pid;

    @JsonIgnore
    public boolean failed = false;

    public static PlayerInventoryRequest of(int playerId) {
        PlayerInventoryRequest par = new PlayerInventoryRequest();
        par.pid = playerId;
        return par;
    }

    public int playerId() {
        return pid;
    }

    @Override
    public String address() {
        return DbVerticle.EVENTBUS_ADDRESS;
    }
}
