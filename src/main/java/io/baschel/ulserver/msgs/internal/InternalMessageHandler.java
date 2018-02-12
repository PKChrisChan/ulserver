package io.baschel.ulserver.msgs.internal;

import io.baschel.ulserver.msgs.InternalServerMessage;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.util.Set;

public interface InternalMessageHandler
{
    void handle(Message<JsonObject> sourceMessage, InternalServerMessage message);
    Set<Class<? extends InternalServerMessage>> handles();
}
