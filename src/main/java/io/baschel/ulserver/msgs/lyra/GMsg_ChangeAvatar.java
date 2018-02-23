package io.baschel.ulserver.msgs.lyra;

public class GMsg_ChangeAvatar implements LyraMessage {
    public int which; // PERMANENT or CURRENT... PERMANENT is literally everything except NMF.
    public LmAvatar avatar;
}
