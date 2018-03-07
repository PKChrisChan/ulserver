package io.baschel.ulserver.msgs.internal;

import io.baschel.ulserver.game.GameVerticle;
import io.baschel.ulserver.game.RoomVerticle;
import io.baschel.ulserver.msgs.InternalServerMessage;

public class LoggedInPlayersRequest implements InternalServerMessage {
    public String dest;

    public static enum LoggedInRequestType {
        GAME,
        LEVEL,
        ROOM
    }

    public LoggedInPlayersRequest() {
    }

    public LoggedInPlayersRequest(LoggedInRequestType type) {
        this(type, 0, 0);
    }

    public LoggedInPlayersRequest(LoggedInRequestType type, int lvl, int room) {
        switch (type) {
            case GAME:
                dest = GameVerticle.INTERNAL_MESSAGE_ADDRESS;
                break;
            case LEVEL:
                dest = RoomVerticle.eventBusAddress(lvl);
                break;
            case ROOM:
                dest = RoomVerticle.eventBusAddress(lvl, room);
                break;
            default:
                dest = GameVerticle.EVENTBUS_ADDRESS;
                break;
        }
    }

    @Override
    public String address() {
        return dest;
    }
}
