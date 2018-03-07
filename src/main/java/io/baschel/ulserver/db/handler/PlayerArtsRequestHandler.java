package io.baschel.ulserver.db.handler;

import io.baschel.ulserver.msgs.InternalServerMessage;
import io.baschel.ulserver.msgs.db.PlayerArtsRequest;
import io.baschel.ulserver.msgs.internal.InternalMessageHandler;
import io.baschel.ulserver.msgs.lyra.LmArts;
import io.baschel.ulserver.util.Json;
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

public class PlayerArtsRequestHandler implements InternalMessageHandler {

    private AsyncSQLClient player;
    private static final Logger L = LoggerFactory.getLogger(PlayerArtsRequestHandler.class);

    public PlayerArtsRequestHandler(AsyncSQLClient pdb) {
        player = pdb;
    }

    @Override
    public void handle(Message<JsonObject> sourceMessage, InternalServerMessage message) {
        player.getConnection(res -> {
            if (res.failed()) {
                L.error("Failed to get connection!", res.cause());
                return;
            }
            if (message instanceof PlayerArtsRequest)
                _handle(res.result(), sourceMessage, (PlayerArtsRequest) message);
        });
    }

    private void _handle(SQLConnection conn, Message<JsonObject> sourceMessage, PlayerArtsRequest message) {
        conn.queryWithParams("select * from skill where player_id=?", new JsonArray().add(message.playerId()), res -> {
            conn.close();
            if (res.failed()) {
                L.error("Unable to fetch arts for {}", res.cause(), message.playerId());
                sourceMessage.fail(-1, res.cause().toString());
            } else {
                LmArts arts = new LmArts();
                res.result().getRows().forEach(jsonObj -> {
                    arts.arts.set(jsonObj.getInteger("skill"), jsonObj.getInteger("skill_level"));
                });

                sourceMessage.reply(Json.objectToJsonObject(arts));
            }
        });
    }

    @Override
    public Set<Class<? extends InternalServerMessage>> handles() {
        return Collections.unmodifiableSet(new HashSet<Class<? extends InternalServerMessage>>() {{
            add(PlayerArtsRequest.class);
        }});
    }
}
