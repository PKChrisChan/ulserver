package io.baschel.ulserver.msgs.lyra;

public class RmRemotePlayer implements LyraMessage {
    public LmPeerUpdate update;
    public LmAvatar avatar;
    public int room;
    public String playername;
}
