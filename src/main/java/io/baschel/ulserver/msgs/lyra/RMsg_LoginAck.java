package io.baschel.ulserver.msgs.lyra;

public class RMsg_LoginAck implements LyraMessage {
    public int status;
    public int roomid;
    public int levelid;
    // TODO MDA not implemented for now. Will need for mares.
    //public int server_ip;
    //public int server_port;
}
