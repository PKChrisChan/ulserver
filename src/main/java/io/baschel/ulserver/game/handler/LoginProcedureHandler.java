package io.baschel.ulserver.game.handler;

import io.baschel.ulserver.Main;
import io.baschel.ulserver.game.MessageHandler;
import io.baschel.ulserver.msgs.MessageUtils;
import io.baschel.ulserver.msgs.lyra.GMsg_Login;
import io.baschel.ulserver.msgs.lyra.GMsg_PreLogin;
import io.baschel.ulserver.msgs.lyra.GMsg_PreLoginAck;
import io.baschel.ulserver.msgs.lyra.LyraMessage;
import io.baschel.ulserver.msgs.lyra.consts.LyraConsts;

import java.util.*;

public class LoginProcedureHandler implements MessageHandler {
    static Set<Class<? extends LyraMessage>> handleSet = new HashSet<>();
    private Map<String, String> pendingConnectionChallengeMap = new HashMap<>();
    static {
        handleSet.add(GMsg_PreLogin.class);
        handleSet.add(GMsg_Login.class);
    }

    @Override
    public void handle(String source, LyraMessage message) {
        if(message instanceof GMsg_PreLogin)
            handlePreLogin(source, (GMsg_PreLogin)message);
        else
            handleLogin(source, (GMsg_Login)message);
    }

    private void handlePreLogin(String source, GMsg_PreLogin message) {
        GMsg_PreLoginAck pla = new GMsg_PreLoginAck();
        pla.version = Main.config.serverConfig.gameVersion;
        if(message.version != Main.config.serverConfig.gameVersion)
            pla.status = LyraConsts.LoginStatus.LOGIN_UNKNOWNERROR.toValue();
        else {
            initChallenge(source);
            pla.status = LyraConsts.LoginStatus.LOGIN_OK.toValue();
            pla.challenge  = pendingConnectionChallengeMap.get(source);
        }

        MessageUtils.sendJsonMessage(source, pla);
    }

    private void handleLogin(String source, GMsg_Login message)
    {

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
