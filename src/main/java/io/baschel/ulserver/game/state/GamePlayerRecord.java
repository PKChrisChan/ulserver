package io.baschel.ulserver.game.state;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.baschel.ulserver.msgs.lyra.InventoryItem;
import io.baschel.ulserver.msgs.lyra.LmArts;
import io.baschel.ulserver.msgs.lyra.LmAvatar;
import io.baschel.ulserver.msgs.lyra.LmStats;
import io.baschel.ulserver.msgs.lyra.consts.LyraConsts;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GamePlayerRecord extends AbstractPlayerRecord {
    public GamePlayerRecord()
    {
        avatar = new LmAvatar();
        arts = new LmArts();
        inventory = new ArrayList<>();
        stats = new LmStats();
    }

    public String password;
    public LyraConsts.Focus focus;

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
    public int timeOnline; // in seconds
    // Pmares only
    public int pmareSessionStart;
    public int pmareBillingType;

    @JsonIgnore
    public boolean isNewlyAwakened()
    {
        return stats.xp < 10000 && timeOnline < (60 * 60 * 2);
    }

    public String locationId()
    {
        return String.format("L%02dR%02d", level, room);
    }

    public String levelId()
    {
        return 'L' + Integer.toString(level);
    }

    public RoomPlayerRecord toRoomPlayerRecord()
    {
        RoomPlayerRecord rpr = new RoomPlayerRecord();
        rpr.avatar = avatar;
        rpr.connectionId = connectionId;
        rpr.name = name;
        rpr.pid = pid;
        rpr.lastUpdate = lastUpdate;
        return rpr;
    }
}
