package io.baschel.ulserver.msgs.lyra;

import java.util.List;

public class GMsg_LoginAck implements LyraMessage {
    public int playerid;
    public short version;
    public short request_status;
    public LmStats stats;
    public LmAvatar avatar;
    public LmArts arts;
    public int xp_gain;
    public int xp_loss;
    public short server_port;
    public short num_items;
    public short max_minutes_online;
    public short session_minutes;
    public short x;
    public short y;
    public int level_id;
    public char gamesite;
    public short ppoints;
    public short pp_pool;
    public int gamesite_id;
    //unsigned int login_time; // UNIX login time, in secs since 1/1/1970
    public String description;
    public List<InventoryItem> items;

}
