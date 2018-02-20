package io.baschel.ulserver.msgs.lyra;

import io.vertx.core.buffer.Buffer;

import java.util.ArrayList;
import java.util.List;

public class RMsg_PlayerUpdate implements LyraMessage {
    public int pid;
    public List<LmPeerUpdate> updates = new ArrayList<>();
    public static final int MSG_TYPE = 1015;

    @Override
    public Buffer asBinary()
    {
        Buffer b = Buffer.buffer();
        b.appendUnsignedInt(pid);
        for(LmPeerUpdate u : updates)
            b.appendBuffer(u.asBinary());
        return b;
    }

    public static List<RMsg_PlayerUpdate> updatesForList(int pid, List<LmPeerUpdate> updateList)
    {
        int current = 0;
        List<RMsg_PlayerUpdate> ret = new ArrayList<>();
        while(current < updateList.size())
        {
            int next = Math.min(current + 100, updateList.size());
            List<LmPeerUpdate> slice = updateList.subList(current, next);
            RMsg_PlayerUpdate u = new RMsg_PlayerUpdate();
            u.updates.addAll(slice);
            u.pid = pid;
            ret.add(u);
            current = next;
        }

        return ret;
    }
}
