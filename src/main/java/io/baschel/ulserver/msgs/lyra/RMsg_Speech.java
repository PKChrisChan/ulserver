package io.baschel.ulserver.msgs.lyra;

public class RMsg_Speech implements LyraMessage {
    public int playerid;
    public String speech_type;
    public boolean babble;
    public int speech_len;
    public String speech_text;
}
