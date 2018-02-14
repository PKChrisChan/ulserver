package io.baschel.ulserver.game;

import io.baschel.ulserver.msgs.lyra.*;
import io.baschel.ulserver.msgs.lyra.consts.LyraConsts;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PlayerRecord {
    public String connectionId;

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
    public List<InventoryItem> inventory;

    public int acctType;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlayerRecord that = (PlayerRecord) o;

        if (pid != that.pid) return false;
        if(!upperName().equals(that.upperName()))
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = connectionId != null ? connectionId.hashCode() : 0;
        result = 31 * result + pid;
        result = 31 * result + upperName().hashCode();
        return result;
    }
}
