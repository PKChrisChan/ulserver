package io.baschel.ulserver.msgs.lyra;

public class RMsg_GotoRoom implements LyraMessage{
    public int roomid;
    public LmPeerUpdate update;
    public int lastx;
    public int lasty;
}
