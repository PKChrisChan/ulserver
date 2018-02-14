package io.baschel.ulserver.msgs.lyra;

public class GMsg_Login implements LyraMessage {
    public long version;                            // client version
    public String playername;
    public String hash;							// md5 hash of server challenge
    public int serv_port;                          // UDP port number for S->C local group updates
    public short pmare_type;							// for pmares, type selected
    public short tcp_only;							// TCP only, for firewalls/NAT/etc.
    public int subversion;                         // another nt to use for version checking
}

