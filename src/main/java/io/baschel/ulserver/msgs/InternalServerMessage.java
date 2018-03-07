package io.baschel.ulserver.msgs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.baschel.ulserver.Main;
import io.baschel.ulserver.util.Json;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public interface InternalServerMessage {
    @JsonIgnore
    default JsonObject getMessageContents() {
        return Json.objectToJsonObject(this);
    }

    default void send() {
        Main.vertx.eventBus().send(address(), Json.objectToJsonObject(this));
    }

    default <T> void send(Handler<AsyncResult<Message<T>>> reply) {
        Main.vertx.eventBus().send(address(), Json.objectToJsonObject(this), reply);
    }

    default <T> void publish() {
        Main.vertx.eventBus().publish(address(), Json.objectToJsonObject(this));
    }

    @JsonIgnore
    String address();
}
