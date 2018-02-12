package io.baschel.ulserver.msgs.lyra;

public class GMsg_PreLoginAck implements LyraMessage {
    public int version;
    public int status;
    public String challenge;
}
