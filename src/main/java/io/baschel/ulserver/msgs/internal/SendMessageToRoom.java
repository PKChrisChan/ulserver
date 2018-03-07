package io.baschel.ulserver.msgs.internal;

import io.baschel.ulserver.game.RoomVerticle;
import io.baschel.ulserver.msgs.InternalServerMessage;
import io.baschel.ulserver.msgs.lyra.LyraMessage;

public class SendMessageToRoom implements InternalServerMessage {

    public int room;
    public int level;
    public LyraMessage message;
    public int sendingPid;
    public boolean sendToSourcePid;

    public SendMessageToRoom() {
        this(-1, -1);
    }

    public SendMessageToRoom(int level, int room) {
        this.level = level;
        this.room = room;
    }

    public SendMessageToRoom setMessage(LyraMessage lm) {
        this.message = lm;
        return this;
    }

    public SendMessageToRoom setSender(int pid) {
        this.sendingPid = pid;
        return this;
    }

    public SendMessageToRoom setSendToSender(boolean send) {
        this.sendToSourcePid = send;
        return this;
    }

    public SendMessageToRoom setLevel(int lvl) {
        this.level = lvl;
        return this;
    }

    public SendMessageToRoom setRoom(int rm) {
        this.room = rm;
        return this;
    }

    @Override
    public String address() {
        if (room > 0 && level > 0)
            return RoomVerticle.eventBusAddress(level, room);
        else if (level > 0)
            return RoomVerticle.eventBusAddress(level);
        else return "";
    }
}
