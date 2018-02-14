package io.baschel.ulserver.game.handler;

import io.baschel.ulserver.Main;
import io.baschel.ulserver.game.GameState;
import io.baschel.ulserver.game.MessageHandler;
import io.baschel.ulserver.game.PlayerRecord;
import io.baschel.ulserver.msgs.MessageUtils;
import io.baschel.ulserver.msgs.db.PlayerRecordRequest;
import io.baschel.ulserver.msgs.lyra.*;
import io.baschel.ulserver.msgs.lyra.consts.LyraConsts;
import io.baschel.ulserver.util.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import sun.plugin2.message.Message;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.baschel.ulserver.msgs.lyra.consts.LyraConsts.GM_DELTA;
import static io.baschel.ulserver.msgs.lyra.consts.LyraConsts.LoginStatus.*;
import static io.baschel.ulserver.msgs.lyra.consts.LyraConsts.PMARE_DELTA;

public class LoginProcedureHandler implements MessageHandler {
    static Set<Class<? extends LyraMessage>> handleSet = new HashSet<>();
    private static final Logger L = LoggerFactory.getLogger(LoginProcedureHandler.class);
    private Map<String, String> pendingConnectionChallengeMap = new HashMap<>();
    static {
        handleSet.add(GMsg_PreLogin.class);
        handleSet.add(GMsg_Login.class);
    }

    private GameState gs;

    public LoginProcedureHandler(GameState gs)
    {
        this.gs = gs;
    }

    @Override
    public void handle(String source, LyraMessage message) {
        if(message instanceof GMsg_PreLogin)
            handlePreLogin(source, (GMsg_PreLogin)message);
        else
            handleLogin(source, (GMsg_Login)message);
    }

    private void handlePreLogin(String source, GMsg_PreLogin message) {
        L.info("Received PreLogin");
        GMsg_PreLoginAck pla = new GMsg_PreLoginAck();
        pla.version = Main.config.serverConfig.gameVersion;
        if(message.version != Main.config.serverConfig.gameVersion) {
            L.warn("Version mismatch: Expected {} got {}", Main.config.serverConfig.gameVersion, message.version);
            pla.status = LyraConsts.LoginStatus.LOGIN_UNKNOWNERROR.toValue();
        }
        else {
            initChallenge(source);
            pla.status = LyraConsts.LoginStatus.LOGIN_OK.toValue();
            pla.challenge  = pendingConnectionChallengeMap.get(source);
        }

        MessageUtils.sendJsonMessage(source, pla);
    }

    private void handleLogin(String source, GMsg_Login message) {
        // First check versions and all that jazz.
        GMsg_LoginAck response = new GMsg_LoginAck();
        AtomicBoolean gmBuild = new AtomicBoolean();
        AtomicBoolean pmareBuild = new AtomicBoolean();
        response.version = (short)Main.config.serverConfig.gameVersion;
        if(message.version > GM_DELTA)
        {
            gmBuild.set(true);
            message.version -= GM_DELTA;
        }
        else if(message.version > PMARE_DELTA)
        {
            pmareBuild.set(true);
            message.version -= PMARE_DELTA;
        }

        if (message.version != Main.config.serverConfig.gameVersion) {
            response.request_status = (short) LOGIN_MISMATCH.toValue().charValue();
            L.warn("Player {} logging in with wrong version. Expected {} got {}", message.playername,
                    Main.config.serverConfig.gameVersion, message.version);
            MessageUtils.sendJsonMessage(source, response);
            return;
        }

        if (message.playername == null || message.playername.length() == 0) {
            response.request_status = (short) LOGIN_USERNOTFOUND.toValue().charValue();
            MessageUtils.sendJsonMessage(source, response);
            L.warn("{} attempting connection with null plyaername", source);
        }

        // Ok - get the playerrecord
        PlayerRecordRequest.of(message.playername).withArts(true).withItems(true).send(result -> {
            if(result.failed())
            {
                L.error("Couldn't retrieve player record for {}", message.playername);
                response.request_status = (short) LOGIN_USERNOTFOUND.toValue().charValue();
                MessageUtils.sendJsonMessage(source, response);
                return;
            }
            else {
                PlayerRecord record = Json.objectFromJsonObject((JsonObject)result.result().body(), PlayerRecord.class);
                record.connectionId = source;
                if(!checkAccount(record, response, gmBuild.get(), pmareBuild.get()))
                {
                    MessageUtils.sendJsonMessage(source, response);
                    return;
                }

                if(Main.config.serverConfig.checkPasswords) {
                    try {
                        if (!checkPassword(record, message.hash)) {
                            response.request_status = (short) LOGIN_BADPASSWORD.toValue().charValue();
                            MessageUtils.sendJsonMessage(source, response);
                            return;
                        }
                    } catch (NoSuchAlgorithmException e) {
                        response.request_status = (short) LOGIN_UNKNOWNERROR.toValue().charValue();
                        MessageUtils.sendJsonMessage(source, response);
                        L.error("Unable to get MessageDigest", e);
                    }
                }
                // TODO MDA: check cooloff, check PMare.
                response.request_status =  (short)LOGIN_OK.toValue().charValue();
                response.avatar = record.avatar;
                response.num_items = (short)record.inventory.size();
                response.items = record.inventory;
                response.stats = record.stats;
                response.server_port = 7500;
                response.gamesite = 'L';
                response.arts = record.arts;
                response.xp_gain = record.xpBonus;
                response.xp_loss = record.xpPenalty;
                response.ppoints = (short)record.stats.pps;
                response.pp_pool = (short)record.stats.pp_pool;
                response.description = record.description;
                response.playerid = record.pid;
                MessageUtils.sendJsonMessage(source, response);
                gs.login(record);
            }
        });
    }

    private boolean checkPassword(PlayerRecord record, String hash) throws NoSuchAlgorithmException {
        String challenge = pendingConnectionChallengeMap.get(record.connectionId);
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] bytes = challenge.getBytes();
        bytes[95] = 0;
        md.update(bytes);
        md.update(Arrays.copyOf(record.password.getBytes(), record.password.length() + 1));
        byte[] mdbytes = md.digest();

        //convert the byte to hex format method
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < mdbytes.length; i++) {
            sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString().toUpperCase().equals(hash);
    }

    private boolean checkAccount(PlayerRecord record, GMsg_LoginAck response, boolean gmBuild, boolean pmareBuild) {
        boolean ok = true;
        if(record.acctType == LyraConsts.AcctType.ACCT_ADMIN_EXPIRED.toValue() ||
                record.acctType == LyraConsts.AcctType.ACCT_PLAYER_EXPIRED.toValue())
        {
            L.warn("Player {} can't log in, status is {}", record.pid, record.acctType);
            response.request_status = (short)LOGIN_EXPIRED.toValue().charValue();
            ok = false;
        }
        else if( record.acctType == LyraConsts.AcctType.ACCT_KILLED.toValue())
        {
            L.warn("Player {} killed", record.pid);
            response.request_status = (short)LOGIN_KILLED.toValue().charValue();
            ok = false;
        }
        else if(record.acctType == LyraConsts.AcctType.ACCT_LOCKED.toValue())
        {
            L.warn("Player {} locked", record.pid);
            response.request_status = (short)LOGIN_TERMINATED.toValue().charValue();
            ok = false;
        }
        else if(gmBuild && record.acctType != LyraConsts.AcctType.ACCT_ADMIN.toValue())
        {
            L.warn("Player {} attempting login with GM build", record.pid);
            response.request_status =  (short)LOGIN_MISMATCH.toValue().charValue();
            ok = false;
        }
        else if(pmareBuild && record.acctType != LyraConsts.AcctType.ACCT_PMARE.toValue())
        {
            L.warn("Player {} attempting to login with PMare build", record.pid);
            response.request_status = (short)LOGIN_MISMATCH.toValue().charValue();
            ok = false;
        }
        else if(gs.isAccountLoggedIn(record.billingId))
        {
            L.warn("Player {} billingId {} already in", record.pid, record.billingId);
            response.request_status = (short)LOGIN_ALREADYIN.toValue().charValue();
            ok = false;
        }
        else if(record.suspendedDate != null && record.suspendedDate.isAfter(LocalDate.now()))
        {
            L.warn("Player {} is suspended", record.pid);
            response.request_status = (short)LOGIN_SUSPENDED.toValue().charValue();
            response.num_items = (short) ChronoUnit.DAYS.between(LocalDate.now(), record.suspendedDate);
        }
        return ok;
    }

    private void initChallenge(String source)
    {
        StringBuffer buf = new StringBuffer();
        for(int i = 0; i < 3; i++)
            buf.append(UUID.randomUUID().toString().replaceAll("-", ""));
        pendingConnectionChallengeMap.put(source, buf.toString());
    }

    @Override
    public Set<Class<? extends LyraMessage>> handles() {
        return Collections.unmodifiableSet(handleSet);
    }
}
