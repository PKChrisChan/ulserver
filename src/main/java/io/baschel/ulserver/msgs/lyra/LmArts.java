package io.baschel.ulserver.msgs.lyra;

import java.util.ArrayList;
import java.util.List;

import static io.baschel.ulserver.msgs.lyra.consts.LyraConsts.NUM_ARTS;

public class LmArts implements LyraMessage {

    public LmArts() {
        arts = new ArrayList<>();
        for (int i = 0; i < NUM_ARTS; i++)
            arts.add(0);
    }

    public List<Integer> arts;
}
