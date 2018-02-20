package io.baschel.ulserver.msgs.lyra;

import io.vertx.core.buffer.Buffer;

public class RMsg_Update implements LyraMessage {
    public static final int MSG_TYPE = 1016;
    public LmPeerUpdate update;
    public int levelid;
    public int roomid;

    public RMsg_Update initFromBinary(Buffer buf)
    {
        update = new LmPeerUpdate().initFromBinary(buf);
        levelid = buf.getUnsignedShort(20);
        roomid = buf.getUnsignedShort(22);
        return this;
    }
}
