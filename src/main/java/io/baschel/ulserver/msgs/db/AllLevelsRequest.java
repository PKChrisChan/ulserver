package io.baschel.ulserver.msgs.db;

import io.baschel.ulserver.db.DbVerticle;
import io.baschel.ulserver.msgs.InternalServerMessage;

public class AllLevelsRequest implements InternalServerMessage {
    @Override
    public String address() {
        return DbVerticle.EVENTBUS_ADDRESS;
    }
}
