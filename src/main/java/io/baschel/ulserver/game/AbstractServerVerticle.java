package io.baschel.ulserver.game;

import io.baschel.ulserver.Main;
import io.baschel.ulserver.msgs.InternalServerMessage;
import io.baschel.ulserver.util.Json;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public abstract class AbstractServerVerticle extends AbstractVerticle {
    @Override
    public void start() {
        vertx.eventBus().consumer(Main.GLOBAL, this::onGlobalMessage);
    }

    private void onGlobalMessage(Message<JsonObject> msg) {
        InternalServerMessage ism = Json.objectFromJsonObject(msg.body(), InternalServerMessage.class);
        handleServerGlobalMessage(ism);
    }

    public abstract void handleServerGlobalMessage(InternalServerMessage ism);
}
