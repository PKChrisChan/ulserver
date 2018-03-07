package io.baschel.ulserver.db.handler;

import io.baschel.ulserver.msgs.InternalServerMessage;
import io.baschel.ulserver.msgs.db.PlayerInventoryRequest;
import io.baschel.ulserver.msgs.internal.InternalMessageHandler;
import io.baschel.ulserver.msgs.lyra.InventoryItem;
import io.baschel.ulserver.msgs.lyra.LmItem;
import io.baschel.ulserver.msgs.lyra.LmItemHdr;
import io.baschel.ulserver.util.Json;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.sql.SQLConnection;

import java.util.*;
import java.util.stream.Collectors;

import static io.baschel.ulserver.msgs.lyra.consts.LyraConsts.OWNER_PLAYER;

public class PlayerInventoryRequestHandler implements InternalMessageHandler {

    private AsyncSQLClient item;
    private static final Logger L = LoggerFactory.getLogger(PlayerInventoryRequestHandler.class);

    public PlayerInventoryRequestHandler(AsyncSQLClient idb) {
        item = idb;
    }

    @Override
    public void handle(Message<JsonObject> sourceMessage, InternalServerMessage message) {
        item.getConnection(res -> {
            if (res.failed()) {
                L.error("Failed to get connection", res.cause());
                return;
            }
            if (message instanceof PlayerInventoryRequest)
                _handle(res.result(), sourceMessage, (PlayerInventoryRequest) message);
        });
    }

    private void _handle(SQLConnection conn, Message<JsonObject> sourceMessage, PlayerInventoryRequest message) {
        conn.queryWithParams("select * from item where owner_id=? and owner_type=?", new JsonArray().add(message.playerId()).add(OWNER_PLAYER), res -> {
            if (res.failed()) {
                sourceMessage.fail(-1, res.cause().toString());
                conn.close();
            } else {
                conn.close();
                List<InventoryItem> inventory = new ArrayList<>();
                for (int i = 0; i < res.result().getNumRows(); i++)
                    inventory.add(null);

                List<InventoryItem> bad = new ArrayList<>();
                res.result().getRows().forEach(jsonObj -> {
                    // Item positions start with 1. Un-fucking-believable.
                    int pos = jsonObj.getInteger("x") - 1;
                    LmItem item = itemFromDbRow(jsonObj);
                    InventoryItem ii = new InventoryItem();
                    ii.item = item;
                    ii.setPosAndFlags(pos + 1);
                    if (ii.pos > inventory.size() || ii.pos < 1) {
                        ii.setPosAndFlags(0);
                        bad.add(ii);
                    } else
                        inventory.set(pos, ii);
                });

                if (bad.size() > 0) {
                    int b = 0;
                    for (int i = 0; i < inventory.size(); i++)
                        if (inventory.get(i) == null) {
                            bad.get(b).setPosAndFlags(i);
                            inventory.set(i, bad.get(b++));
                        }
                }

                sourceMessage.reply(new JsonArray(inventory.stream().map(it -> Json.objectToJsonObject(it)).collect(Collectors.toList())));
            }
        });
    }

    public LmItem itemFromDbRow(JsonObject row) {
        LmItem item = new LmItem();
        item.header = new LmItemHdr();
        // this isn't a typo, it's just a result of the fuckendess of Lyra's db.
        // LmItemHdr's itemid is the item_hdr in the database which is all the flags and shit on the header (i.e. graphic,
        // colors, etc.)
        // LmItemHdr's serial is item_id in the db which is a unique id for an item.
        item.header.itemid = row.getInteger("item_hdr");
        item.header.serial = row.getInteger("item_id");
        item.state1 = row.getInteger("item_state1");
        item.state2 = row.getInteger("item_state2");
        item.state3 = row.getInteger("item_state3");
        item.name = row.getString("item_name");

        return item;
    }

    @Override
    public Set<Class<? extends InternalServerMessage>> handles() {
        return Collections.unmodifiableSet(new HashSet<Class<? extends InternalServerMessage>>() {{
            add(PlayerInventoryRequest.class);
        }});
    }
}
