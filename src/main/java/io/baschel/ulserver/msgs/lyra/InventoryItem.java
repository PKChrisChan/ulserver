package io.baschel.ulserver.msgs.lyra;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Item that belongs to a player inventory.
 * Not really a Lyra type. Just a wrapper for my sanity.
 */
public class InventoryItem {
    public LmItem item;
    public int pos; // Position in the items list
    public int flags; // 1 means identified, 2 means active shield

    @JsonIgnore
    public void setPosAndFlags(int dbXValue)
    {
        pos = dbXValue & 0xFF;
        flags = (dbXValue >> 8) & 0xFF;
    }

    @JsonIgnore
    public int getDbXValue()
    {
        int x = 0;
        x =  flags << 8;
        x |= pos;
        return x;
    }
}
