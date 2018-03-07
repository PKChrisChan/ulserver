package io.baschel.ulserver.msgs.lyra;

import java.util.ArrayList;
import java.util.List;

public class GMsg_ChangeStat implements LyraMessage {
    public static class StatChange {
        public int stat;
        public int requesttype;
        public int value;
    }

    public List<StatChange> changes;

    public GMsg_ChangeStat() {
        changes = new ArrayList<>();
    }
}
