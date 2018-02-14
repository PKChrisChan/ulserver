package io.baschel.ulserver.db.handler;

import io.baschel.ulserver.game.PlayerRecord;
import io.baschel.ulserver.msgs.InternalServerMessage;
import io.baschel.ulserver.msgs.db.*;
import io.baschel.ulserver.msgs.internal.InternalMessageHandler;
import io.baschel.ulserver.msgs.lyra.InventoryItem;
import io.baschel.ulserver.msgs.lyra.LmArts;
import io.baschel.ulserver.msgs.lyra.LmItem;
import io.baschel.ulserver.msgs.lyra.LmStats;
import io.baschel.ulserver.msgs.lyra.consts.LyraConsts;
import io.baschel.ulserver.util.Json;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.sql.SQLConnection;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PlayerRecordRequestHandler implements InternalMessageHandler {

    private AsyncSQLClient player;
    public PlayerRecordRequestHandler(AsyncSQLClient playerDb)
    {
        player = playerDb;
    }
    private static final Logger L = LoggerFactory.getLogger(PlayerRecordRequestHandler.class);

    @Override
    public void handle(Message<JsonObject> sourceMessage, InternalServerMessage message) {
        player.getConnection(res-> {
            if(res.failed())
            {
                L.error("Failed to connect!", res.cause());
                return;
            }
            if (message instanceof PlayerRecordRequest)
                _handle(res.result(), sourceMessage, (PlayerRecordRequest) message);
            else if (message instanceof PlayerStatsRequest)
                _handle(res.result(), sourceMessage, (PlayerStatsRequest) message);
            else if (message instanceof PlayerGuildRankRequest)
                _handle(res.result(), sourceMessage, (PlayerGuildRankRequest) message);
        });
    }

    public void fillInPlayerRecordFromPlayerDbRow(PlayerRecord record, JsonObject row)
    {
        // Build the record
        record.pid = row.getInteger("player_id");
        record.name = row.getString("player_name");
        record.password = row.getString("password");
        record.focus = LyraConsts.Focus.fromValue(row.getInteger("focus_stat"));
        record.avatar.avatar1 = row.getInteger("avatar");
        record.avatar.avatar2 = row.getInteger("avatar2");
        record.stats.xp = row.getInteger("xp");
        record.stats.orbit = LmStats.OrbitFromXP(record.stats.xp);
        record.stats.focus = record.focus.toValue();
        record.acctType = LyraConsts.AcctType.fromValue(row.getInteger("acct_type")).toValue();
        record.billingId = row.getInteger("billing_id");
        record.description = row.getString("avatar_descrip");
        record.xpBonus = row.getInteger("xp_bonus");
        record.xpPenalty = row.getInteger("xp_penalty");
        String suspendedDate = row.getString("suspended_date");
        if(suspendedDate != null)
            record.suspendedDate = LocalDate.from(DateTimeFormatter.ISO_LOCAL_DATE.parse(suspendedDate));

        record.pmareSessionStart = row.getInteger("pmare_session_start");
        record.pmareBillingType = row.getInteger("pmare_billing_type");
        record.stats.pps = row.getInteger("pps");
        record.stats.pp_pool = row.getInteger("pp_pool");
        record.stats.quest_xp_pool = row.getInteger("quest_xp_pool");
        String lastLogout = row.getString("last_logout");
        if(lastLogout != null)
            record.lastLogout = LocalDateTime.from(DateTimeFormatter.ISO_DATE_TIME.parse(lastLogout));

        // and fuck all the rest.
    }

    private void _handle(SQLConnection conn, Message<JsonObject> sourceMessage, PlayerRecordRequest recordRequest)
    {
        String whereClause = recordRequest.byId() ? "where player_id = ?" : "where upper_name = ?";
        PlayerRecord record = new PlayerRecord();
        JsonArray params = new JsonArray().add(recordRequest.byId() ? recordRequest.id() : recordRequest.uname());
        conn.queryWithParams("select player_id,player_name,password,focus_stat,avatar,avatar2,xp," +
                "acct_type,billing_id,xp_bonus,xp_penalty,avatar_descrip,suspended_date,pmare_session_start,pmare_billing_type,quest_xp_pool," +
                "pps,pp_pool,last_logout from player " + whereClause, params, res -> {
            conn.close();
            if(res.failed()) {
                L.error("Failed to retrieve player record for {}", res.cause(), params.encode());
                recordRequest.failed = true;
                sourceMessage.fail(-1, res.cause().toString());
            }
            else {
                if(res.result().getNumRows() == 0) {
                    L.error("Failed to retrieve player record for {} - numrows was 0", params.encode());
                    recordRequest.failed = true;
                    sourceMessage.fail(0, "Unable to locate PlayerRecord!");
                }
                else if(res.result().getNumRows() != 1) {
                    L.error("Failed to retrieve player record for {} - numrows was > 1", params.encode());
                    sourceMessage.fail(1, "Ambiguous PlayerRecord response!");
                    recordRequest.failed = true;
                }
                else {
                    JsonObject row = res.result().getRows().get(0);
                    fillInPlayerRecordFromPlayerDbRow(record, row);
                    getGuildRanks(record, recordRequest, sourceMessage);
                    getStats(record, recordRequest, sourceMessage);

                    recordRequest.gotRecord = true;
                    if(recordRequest.withArts())
                        getArts(record, recordRequest, sourceMessage);

                    if(recordRequest.withItems())
                        getItems(record, recordRequest, sourceMessage);
                }
            }
        });
    }

    private void _handle(SQLConnection conn, Message<JsonObject> sourceMessage, PlayerGuildRankRequest pgrr)
    {
        conn.queryWithParams("select * from guildplayer where player_id=? and rank > 0", new JsonArray().add(pgrr.playerId()), res -> {
            conn.close();
            if(res.failed()) {
                L.error("Failed to retrieve from guildplayer for {}", res.cause(), pgrr.pid);
                sourceMessage.fail(5, "Failed to retrieve guildranks!");
            }
            else {
                LmStats guildStats = new LmStats();
                res.result().getRows().forEach(guildrow -> {
                    guildStats.xp_pool.set(guildrow.getInteger("guild_id"), guildrow.getInteger("xp_pool_curr"));
                    guildStats.rank.set(guildrow.getInteger("guild_id"), guildrow.getInteger("rank"));
                });
                sourceMessage.reply(Json.objectToJsonObject(guildStats));
            }
        });
    }

    private void _handle(SQLConnection conn, Message<JsonObject> sourceMessage, PlayerStatsRequest psr)
    {
        conn.queryWithParams("select * from stat where player_id=?", new JsonArray().add(psr.playerId()), res -> {
            conn.close();
            if(res.failed()) {
                L.error("Failed to retrieve from stat for {}", res.cause(), psr.pid);
                sourceMessage.fail(5, "Failed to retrieve stats!");
            }
            else {
                LmStats stats = new LmStats();
                res.result().getRows().forEach(statrow -> {
                    stats.curr.set(statrow.getInteger("stat"), statrow.getInteger("curr_stat_level"));
                    stats.max.set(statrow.getInteger("stat"), statrow.getInteger("max_stat_level"));
                });
                sourceMessage.reply(Json.objectToJsonObject(stats));
            }
        });
    }


    private void getArts(PlayerRecord rec, PlayerRecordRequest origreq, Message<JsonObject> msg)
    {
        PlayerArtsRequest artsReq = PlayerArtsRequest.of(rec.pid);
        artsReq.send(reply -> {
            if (reply.failed()) {
                origreq.failed = true;
                msg.fail(2, "Unable to retrieve arts");
            }
            else {
                LmArts arts = Json.objectFromJsonObject((JsonObject) reply.result().body(), LmArts.class);
                rec.arts = arts;
                origreq.gotArts = true;
                checkComplete(origreq, rec, msg);
            }
        });
    }

    private void getGuildRanks(PlayerRecord rec, PlayerRecordRequest req, Message<JsonObject> msg)
    {
        PlayerGuildRankRequest pgrr = PlayerGuildRankRequest.of(rec.pid);
        pgrr.send(reply -> {
            if(reply.failed())
            {
                req.failed = true;
                msg.fail(6, "Unable to retrieve guildRanks");
            } else {
                LmStats stats = Json.objectFromJsonObject((JsonObject)reply.result().body(), LmStats.class);
                rec.stats.rank = stats.rank;
                rec.stats.xp_pool = stats.xp_pool;
                req.gotGuildRanks = true;
                checkComplete(req, rec, msg);
            }
        });
    }

    private void getStats(PlayerRecord rec, PlayerRecordRequest req, Message<JsonObject> msg)
    {
        PlayerStatsRequest psr = PlayerStatsRequest.of(rec.pid);
        psr.send(reply -> {
            if(reply.failed())
            {
                req.failed = true;
                msg.fail(6, "Unable to retrieve stats");
            } else {
                LmStats stats = Json.objectFromJsonObject((JsonObject)reply.result().body(), LmStats.class);
                rec.stats.curr = stats.curr;
                rec.stats.max = stats.max;
                req.gotStats = true;
                checkComplete(req, rec, msg);
            }
        });
    }


    private void checkComplete(PlayerRecordRequest req, PlayerRecord rec, Message<JsonObject> msg)
    {
        if(req.isComplete() && !req.failed)
            msg.reply(Json.objectToJsonObject(rec));
    }

    private void getItems(PlayerRecord rec, PlayerRecordRequest req, Message<JsonObject> msg)
    {
        PlayerInventoryRequest pir = PlayerInventoryRequest.of(rec.pid);
        pir.send(reply -> {
           if(reply.failed()) {
               L.error("Failed to retrieve items for {}", reply.cause(), rec.pid);
               req.failed = true;
               msg.fail(3, "Unable to retrieve items");
           }
           else {
               JsonArray items = (JsonArray)reply.result().body();
               List<InventoryItem> inventory = (List<InventoryItem>)items.getList().stream().map(o -> Json.objectFromJsonObject((JsonObject)o, InventoryItem.class)).collect(Collectors.toList());
               rec.inventory = inventory;
               req.gotItems = true;
               checkComplete(req, rec, msg);
           }
        });
    }

    @Override
    public Set<Class<? extends InternalServerMessage>> handles() {
        return Collections.unmodifiableSet(new HashSet<Class<? extends InternalServerMessage>>() {{
            add(PlayerRecordRequest.class);
            add(PlayerGuildRankRequest.class);
            add(PlayerStatsRequest.class);
        }});
    }
}
