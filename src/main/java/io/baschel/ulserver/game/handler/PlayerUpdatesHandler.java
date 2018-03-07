package io.baschel.ulserver.game.handler;

import io.baschel.ulserver.game.GameState;
import io.baschel.ulserver.game.state.AbstractPlayerRecord;
import io.baschel.ulserver.game.state.GamePlayerRecord;
import io.baschel.ulserver.msgs.internal.SendMessageToRoom;
import io.baschel.ulserver.msgs.lyra.*;
import io.baschel.ulserver.msgs.lyra.consts.LyraConsts;
import io.baschel.ulserver.util.Ref;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

import static io.baschel.ulserver.msgs.lyra.consts.LyraConsts.Guild.NO_GUILD;
import static io.baschel.ulserver.msgs.lyra.consts.LyraConsts.Guild.NO_RANK;
import static io.baschel.ulserver.msgs.lyra.consts.LyraConsts.StatChangeTypes.*;

public class PlayerUpdatesHandler extends GameMessageHandler {
    private static final Logger L = LoggerFactory.getLogger(PlayerUpdatesHandler.class);

    public PlayerUpdatesHandler(GameState gs) {
        super(gs);
    }

    @Override
    public void handle(String source, LyraMessage message) {
        if (message instanceof GMsg_ChangeStat)
            _handle(source, (GMsg_ChangeStat) message);
        if (message instanceof GMsg_ChangeAvatar)
            _handle(source, (GMsg_ChangeAvatar) message);
    }

    private void _handle(String source, GMsg_ChangeStat message) {
        GamePlayerRecord rec = gs.playerRecordForConnectionId(source);
        Ref<Boolean> b = new Ref(true);
        message.changes.forEach(sc -> {
            switch (sc.requesttype) {
                case SET_XP:
                case SET_STAT_MAX:
                    handleIllegalStatChange(rec, sc);
                    b.set(false);
                    break;
                case SET_STAT_CURR:
                    handleSetStatCurr(rec, sc);
                    break;
                case SET_SKILL:
                    b.set(b.get() && handleSetSkill(rec, sc));
                    break;
                default:
                    L.warn("Unknown requestType {} in changeStat message", sc.requesttype);
                    b.set(false);
                    break;
            }
        });

        if (b.get()) {
            gs.sendToPlayer(message, rec);
        }
    }

    private void _handle(String source, GMsg_ChangeAvatar message) {
        GamePlayerRecord rec = gs.playerRecordForConnectionId(source);
        JsonObject av = message.avatar.toJsonObject();
        // fixup the avatar...
        if (av.getInteger("teacher") > 0 && rec.arts.arts.get(LyraConsts.Arts.TRAIN.ordinal()) == 0)
            av.put("teacher", 0);
        av.put("master_teacher", rec.arts.arts.get(LyraConsts.Arts.TRAIN_SELF.ordinal()) > 0 && av.getInteger("teacher") > 0 ? 1 : 0);
        if(av.getInteger("show_sphere") > 0) {
            av.put("dreamstrike", rec.arts.arts.get(LyraConsts.Arts.DREAMSTRIKE.ordinal()) > 0 ? 1 : 0);
            av.put("dreamsmith", rec.arts.arts.get(LyraConsts.Arts.DREAMSMITH_MARK.ordinal()) > 0 ? 1 : 0);
            av.put("wordsmith", rec.arts.arts.get(LyraConsts.Arts.WORDSMITH_MARK.ordinal()) > 0 ? 1 : 0);
        }
        if (rec.acctType != LyraConsts.AcctType.ACCT_ADMIN.toValue()) {
            av.put("np_symbol", 0);
            av.put("show_lyran", 0);
            av.put("hidden", 0);
        }

        av.put("sphere", rec.stats.orbit / 10);
        if(av.getInteger("guild_id") != NO_GUILD)
            av.put("guild_rank", NO_RANK);
        else
        if (av.getInteger("guild_rank") == NO_RANK)
            av.put("guild_id", NO_GUILD);

        RMsg_ChangeAvatar rmChange = new RMsg_ChangeAvatar();
        LmAvatar avMsg = new LmAvatar();
        avMsg.fromJsonObject(av);
        rmChange.avatar = avMsg;
        rmChange.playerid = rec.pid;
        rec.avatar = avMsg;
        // TODO MDA send to party!
        new SendMessageToRoom(rec.level, rec.room).setSendToSender(false).setSender(rec.pid).setMeesage(rmChange).send();
    }

    private boolean handleSetSkill(GamePlayerRecord rec, GMsg_ChangeStat.StatChange sc) {
        // Only certain skills can be sent by the client - verify them here.
        if (rec.arts.arts.get(sc.stat) == sc.value)
            return true;

        if ((rec.arts.arts.get(sc.stat) == 0 && sc.value > 0 || sc.value % 10 == 0) && !LyraConsts.Arts.fromOrdinal(sc.stat).isAutoTrainableGuildArt()) {
            L.warn("Player {} sending illegal imp in art {} (old level: {} new level: {})", rec.pid, sc.stat, rec.arts.arts.get(sc.stat), sc.value);
            return false;
        }

        rec.arts.arts.set(sc.stat, sc.value);
        return true;
    }

    private void handleSetStatCurr(GamePlayerRecord rec, GMsg_ChangeStat.StatChange sc) {
        if (rec.stats.max.get(sc.stat) < sc.value) {
            L.warn("Player {} sending illegal current level in stat {} (max: {}, curr: {})", rec.pid, sc.stat, rec.stats.max.get(sc.stat), sc.value);
            return;
        }

        rec.stats.curr.set(sc.stat, sc.value);
    }

    private void handleIllegalStatChange(AbstractPlayerRecord rec, GMsg_ChangeStat.StatChange sc) {
        L.warn("Player {} sending illegal stat change (type: {})", rec.pid);
    }

    @Override
    public Set<Class<? extends LyraMessage>> handles() {
        return new HashSet<Class<? extends LyraMessage>>() {{
            add(GMsg_ChangeStat.class);
            add(GMsg_ChangeAvatar.class);
        }};
    }
}
