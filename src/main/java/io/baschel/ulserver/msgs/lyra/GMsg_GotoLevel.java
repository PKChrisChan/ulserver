package io.baschel.ulserver.msgs.lyra;

public class GMsg_GotoLevel implements LyraMessage {
    public int levelid;
    public int roomid;
    public LmPeerUpdate update;
}
