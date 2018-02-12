package io.baschel.ulserver.db;

import io.baschel.ulserver.Main;
import io.baschel.ulserver.config.DbConfig;
import io.baschel.ulserver.msgs.InternalServerMessage;
import io.baschel.ulserver.util.Json;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.SQLConnection;

import java.util.HashMap;
import java.util.Map;

public class DbVerticle extends AbstractVerticle {

    private Map<String, SQLConnection> dbConns = new HashMap<>();
    private Map<String, AsyncSQLClient> clientsForDb = new HashMap<>();
    private static final Logger L = LoggerFactory.getLogger(DbVerticle.class);
    public static final String EVENTBUS_ADDRESS = DbVerticle.class.getName();
    private DbMessageRouter messageRouter = new DbMessageRouter();
    @Override
    public void start()
    {
        connect();
        vertx.eventBus().consumer(EVENTBUS_ADDRESS, this::handleDbMessage);
    }

    private void handleDbMessage(Message<JsonObject> message) {
        JsonObject body = message.body();
        InternalServerMessage ism = Json.objectFromJsonObject(body, InternalServerMessage.class);
        messageRouter.handle(message, ism);
    }

    public void connect()
    {
        DbConfig dbc = Main.config.dbConfig;
        dbc.databases.forEach((db, pass) -> {
            JsonObject dbProps = new JsonObject()
                    .put("host", dbc.host)
                    .put("port", dbc.port)
                    .put("username", db)
                    .put("password", pass)
                    .put("database", db);
            AsyncSQLClient client = MySQLClient.createNonShared(vertx, dbProps);
            clientsForDb.put(db, client);
        });
        messageRouter.setDbClients(clientsForDb);
        messageRouter.registerHandlers();
    }
}
