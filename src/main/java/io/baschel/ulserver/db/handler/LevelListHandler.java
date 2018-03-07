package io.baschel.ulserver.db.handler;

import io.baschel.ulserver.msgs.InternalServerMessage;
import io.baschel.ulserver.msgs.db.AllLevelsRequest;
import io.baschel.ulserver.msgs.internal.InternalMessageHandler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.sql.SQLConnection;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class LevelListHandler implements InternalMessageHandler {

    private AsyncSQLClient level;
    private static final Logger L = LoggerFactory.getLogger(LevelListHandler.class);

    public LevelListHandler(AsyncSQLClient ldb) {
        level = ldb;
    }

    @Override
    public void handle(Message<JsonObject> sourceMessage, InternalServerMessage message) {
        level.getConnection(res -> {
            if (res.failed()) {
                L.error("Failed to connect!", res.failed());
                return;
            }

            if (message instanceof AllLevelsRequest)
                _handle(res.result(), sourceMessage, (AllLevelsRequest) message);
        });
    }

    private void _handle(SQLConnection conn, Message<JsonObject> sourceMessage, AllLevelsRequest message) {
        conn.query("select level_id,room_id,no_reap from room", res -> {
            if (res.failed()) {
                L.error("Failed to fetch levels", res.cause());
                return;
            }

            sourceMessage.reply(new JsonArray(res.result().getRows()));
        });
    }

    @Override
    public Set<Class<? extends InternalServerMessage>> handles() {
        return Collections.unmodifiableSet(new HashSet<Class<? extends InternalServerMessage>>() {{
            add(AllLevelsRequest.class);
        }});
    }
}
