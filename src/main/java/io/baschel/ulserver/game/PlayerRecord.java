package io.baschel.ulserver.game;

import io.baschel.ulserver.msgs.lyra.LmArts;
import io.baschel.ulserver.msgs.lyra.LmAvatar;
import io.baschel.ulserver.msgs.lyra.LmItem;
import io.baschel.ulserver.msgs.lyra.LmStats;
import io.baschel.ulserver.msgs.lyra.consts.LyraConsts;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PlayerRecord {
    public PlayerRecord()
    {
        avatar = new LmAvatar();
        arts = new LmArts();
        inventory = new ArrayList<>();
        stats = new LmStats();
    }
    public int pid;
    public String name;
    public String password;
    public LyraConsts.Focus focus;

    public LmAvatar avatar;
    public LmArts arts;
    public LmStats stats;
    public List<LmItem> inventory;

    public LyraConsts.AcctType acctType;
    public int billingId;
    public String description;

    public LocalDateTime lastLogout;
    public LocalDate suspendedDate;

    // Used by locate
    public int level;
    public int room;
    // These are used at login only
    public int xpBonus;
    public int xpPenalty;

    // Pmares only
    public int pmareSessionStart;
    public int pmareBillingType;

    public String upperName()
    {
        return name.toUpperCase();
    }

    public String locationId()
    {
        StringBuffer sb = new StringBuffer();
        if(level != 0) {
            sb.append('L' + Integer.toString(level));
            sb.append('R' + Integer.toString(room));
        }
        return sb.toString();
    }
}
