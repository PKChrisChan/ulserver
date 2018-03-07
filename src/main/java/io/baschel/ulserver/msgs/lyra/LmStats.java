package io.baschel.ulserver.msgs.lyra;

import io.baschel.ulserver.msgs.lyra.consts.LyraConsts;

import java.util.ArrayList;
import java.util.List;

import static io.baschel.ulserver.msgs.lyra.consts.LyraConsts.*;

public class LmStats {
    public int focus;
    public int orbit;
    public int xp;
    public int quest_xp_pool;
    public int pps;
    public int pp_pool;
    // Current and max of focus elements
    public List<Integer> curr;
    public List<Integer> max;
    // Rank and XP Pool of each guild
    public List<Integer> rank;
    public List<Integer> xp_pool;

    public static int OrbitFromXP(int xp) {
        LyraConsts.XpTableEntry[] table = XP_PER_ORBIT;
        int i;
        for (i = 0; i < table.length; i++) {
            if (xp < table[i].baseXpForSphere) {
                break;
            }
        }
        LyraConsts.XpTableEntry entry = table[i - 1];
        int sphereXp = entry.baseXpForSphere;
        int orb = 0;
        for (orb = 0; orb < 10; orb++) {
            sphereXp += entry.xpPerOrb;
            if (sphereXp > xp)
                break;
        }
        return Math.min((entry.sphere * 10) + orb, 99);
    }

    public LmStats() {
        curr = new ArrayList<>();
        max = new ArrayList<>();
        rank = new ArrayList<>();
        xp_pool = new ArrayList<>();

        for (int i = 0; i < NUM_PLAYER_STATS; i++) {
            curr.add(0);
            max.add(0);
        }

        for (int i = 0; i < NUM_GUILDS; i++) {
            rank.add(0);
            xp_pool.add(0);
        }
    }
}
