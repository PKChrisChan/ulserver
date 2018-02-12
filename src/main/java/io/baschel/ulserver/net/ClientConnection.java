package io.baschel.ulserver.net;

import io.baschel.ulserver.Main;
import io.baschel.ulserver.game.GameVerticle;
import io.baschel.ulserver.msgs.MessageUtils;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import static io.baschel.ulserver.net.ClientConnection.ConnectionState.ChallengeSent;
import static io.baschel.ulserver.net.ClientConnection.ConnectionState.NewConnection;

public class ClientConnection {

    public enum ConnectionState
    {
        NewConnection, // All connections start in this state
        ChallengeSent, // PreLogin has been processed and a challenge has been sent, awaiting Login
        Active, // Connection is logged in
        Idle, // Idle
        Dead
    }

    private long connectionTime;
    private long lastMessageTime;
    private String cxnId;
    private ConnectionState state;
    private byte[] challenge;

    public ClientConnection(String socketAddress)
    {
        this.cxnId = socketAddress;
        this.state = NewConnection;
        this.connectionTime = System.currentTimeMillis();
    }

    public void setChallenge(byte[] chal)
    {
        this.challenge = chal;
        this.state = ChallengeSent;
    }

    public void handleMessage(Buffer buffer)
    {
        this.lastMessageTime = System.currentTimeMillis();
        JsonObject msg = MessageUtils.readRawSocketMessage(buffer);
        JsonObject publishable = new JsonObject()
                .put("origin", cxnId)
                .put("payload", msg);
        Main.vertx.eventBus().publish(GameVerticle.EVENTBUS_ADDRESS, publishable);
    }
}
