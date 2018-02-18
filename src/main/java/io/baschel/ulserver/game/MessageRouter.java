package io.baschel.ulserver.game;

import io.baschel.ulserver.game.handler.LocationChangeHandler;
import io.baschel.ulserver.game.handler.LoginProcedureHandler;
import io.baschel.ulserver.game.handler.PingHandler;
import io.baschel.ulserver.msgs.lyra.LyraMessage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MessageRouter
{
    private GameState gameState;
    private Map<Class<? extends LyraMessage>, Set<MessageHandler>> messageHandlerMap;

    public MessageRouter(GameState state)
    {
        messageHandlerMap = new HashMap<>();
        gameState = state;
        registerHandlers();
    }

    public void registerHandlers()
    {
        addHandler(new LoginProcedureHandler(gameState));
        addHandler(new PingHandler(gameState));
        addHandler(new LocationChangeHandler(gameState));
    }

    private void addHandler(MessageHandler handler) {
        Set<Class<? extends LyraMessage>> handles = handler.handles();
        handles.forEach(c -> {
            messageHandlerMap.computeIfAbsent(c, k -> new HashSet<>());
            messageHandlerMap.get(c).add(handler);
        });
    }

    public void handle(String source, LyraMessage message)
    {
        Set<MessageHandler> handlers = messageHandlerMap.get(message.getClass());
        if(handlers != null)
            handlers.forEach(h -> h.handle(source, message));
    }
}
