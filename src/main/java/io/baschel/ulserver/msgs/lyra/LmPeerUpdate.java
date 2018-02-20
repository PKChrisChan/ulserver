package io.baschel.ulserver.msgs.lyra;

import io.vertx.core.buffer.Buffer;

public class LmPeerUpdate implements LyraMessage {
    public int playerid;
    public byte sound_id;
    public short x;
    public short y;
    public int u1;
    public int u2;

    public LmPeerUpdate initFromBinary(Buffer buf)
    {
        playerid = buf.getInt(0);
        x = buf.getShort(4);
        y = buf.getShort(6);
        u1 = buf.getInt(8);
        u2 = buf.getInt(12);
        sound_id = buf.getByte(16);
        // 3 unused bytes!
        return this;
    }

    @Override
    public Buffer asBinary()
    {
        Buffer b = Buffer.buffer();
        b.appendUnsignedInt(playerid);
        b.appendShort(x);
        b.appendShort(y);
        b.appendUnsignedInt(u1);
        b.appendUnsignedInt(u2);
        b.appendByte(sound_id);
        byte z = 0;
        b.appendByte(z);
        b.appendByte(z);
        b.appendByte(z);
        return b;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LmPeerUpdate that = (LmPeerUpdate) o;

        if (playerid != that.playerid) return false;
        if (sound_id != that.sound_id) return false;
        if (x != that.x) return false;
        if (y != that.y) return false;
        if (u1 != that.u1) return false;
        return u2 == that.u2;
    }

    @Override
    public int hashCode() {
        int result = playerid;
        result = 31 * result + (int) sound_id;
        result = 31 * result + (int) x;
        result = 31 * result + (int) y;
        result = 31 * result + u1;
        result = 31 * result + u2;
        return result;
    }
}
