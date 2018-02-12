package io.baschel.ulserver.db;

import io.baschel.ulserver.db.handler.PlayerArtsRequestHandler;
import io.baschel.ulserver.db.handler.PlayerInventoryRequestHandler;
import io.baschel.ulserver.db.handler.PlayerRecordRequestHandler;
import io.baschel.ulserver.msgs.InternalServerMessage;
import io.baschel.ulserver.msgs.internal.InternalMessageHandler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.sql.SQLConnection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DbMessageRouter {
    private Map<Class<? extends InternalServerMessage>, Set<InternalMessageHandler>> messageHandlerMap;
    private Map<String, AsyncSQLClient> dbClients;

    public DbMessageRouter()
    {
        messageHandlerMap = new HashMap<>();
    }

    public void registerHandlers()
    {
        addHandler(new PlayerRecordRequestHandler(dbClients.get("ul_player")));
        addHandler(new PlayerArtsRequestHandler(dbClients.get("ul_player")));
        addHandler(new PlayerInventoryRequestHandler(dbClients.get("ul_item")));
    }

    private void addHandler(InternalMessageHandler handler) {
        Set<Class<? extends InternalServerMessage>> handles = handler.handles();
        handles.forEach(c -> {
            messageHandlerMap.computeIfAbsent(c, k -> new HashSet<>());
            messageHandlerMap.get(c).add(handler);
        });
    }

    public void handle(Message<JsonObject> source, InternalServerMessage message)
    {
        Set<InternalMessageHandler> handlers = messageHandlerMap.get(message.getClass());
        if(handlers != null)
            handlers.forEach(h -> h.handle(source, message));
    }

    public void setDbClients(Map<String,AsyncSQLClient> dbClients) {
        this.dbClients = dbClients;
    }
}