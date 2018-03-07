package io.baschel.ulserver.msgs.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.baschel.ulserver.db.DbVerticle;
import io.baschel.ulserver.msgs.InternalServerMessage;

public class PlayerRecordRequest implements InternalServerMessage {
    public String playerName = "";
    public int playerId = -1;
    public boolean withItems = false;
    public boolean withArts = false;

    @JsonIgnore
    public boolean failed = false;

    @JsonIgnore
    public boolean gotRecord = false;

    @JsonIgnore
    public boolean gotItems = false;

    @JsonIgnore
    public boolean gotArts = false;

    @JsonIgnore
    public boolean gotGuildRanks = false;

    @JsonIgnore
    public boolean gotStats = false;

    @JsonIgnore
    public synchronized boolean isComplete() {
        return gotRecord && (!withArts || gotArts) && (!withItems || gotItems) && gotGuildRanks && gotStats;
    }

    public static PlayerRecordRequest of(String playerName) {
        PlayerRecordRequest prr = new PlayerRecordRequest();
        prr.playerName = playerName;
        return prr;
    }

    public static PlayerRecordRequest of(int pid) {
        PlayerRecordRequest prr = new PlayerRecordRequest();
        prr.playerId = pid;
        return prr;
    }

    public boolean byName() {
        return playerId == -1 && playerName.length() > 0;
    }

    public boolean byId() {
        return playerId != -1;
    }

    public int id() {
        return playerId;
    }

    public String name() {
        return playerName;
    }

    public String uname() {
        return name().toUpperCase();
    }

    public PlayerRecordRequest withItems(boolean items) {
        withItems = items;
        return this;
    }

    public PlayerRecordRequest withArts(boolean arts) {
        withArts = arts;
        return this;
    }

    public boolean withArts() {
        return withArts;
    }

    public boolean withItems() {
        return withItems;
    }

    @Override
    public String address() {
        return DbVerticle.EVENTBUS_ADDRESS;
    }
}
