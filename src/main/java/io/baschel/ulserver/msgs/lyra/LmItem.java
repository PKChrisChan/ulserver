package io.baschel.ulserver.msgs.lyra;

public class LmItem implements LyraMessage {
    public LmItemHdr header;
    public int state1;
    public int state2;
    public int state3;
    public String name;

    public LmItem() {
        header = new LmItemHdr();
    }
}
