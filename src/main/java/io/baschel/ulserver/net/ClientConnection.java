package io.baschel.ulserver.net;

import io.baschel.ulserver.Main;
import io.baschel.ulserver.game.GameVerticle;
import io.baschel.ulserver.msgs.MessageUtils;
import io.baschel.ulserver.msgs.internal.DisconnectClient;
import io.baschel.ulserver.util.Json;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import static io.baschel.ulserver.net.ClientConnection.ConnectionState.ChallengeSent;
import static io.baschel.ulserver.net.ClientConnection.ConnectionState.NewConnection;

public class ClientConnection {
    public static final int HEADER_LEN = 4;
    private static Logger L = LoggerFactory.getLogger(ClientConnection.class);

    public void onClose(Void v)
    {
        L.debug("Received close for connection {}", cxnId);
        Main.vertx.eventBus().send(GameVerticle.INTERNAL_MESSAGE_ADDRESS, Json.objectToJsonObject(new DisconnectClient(cxnId)));
    }

    public static class MessageHeader
    {
        public int type;
        public int size;

        MessageHeader(int t, int s)
        {
            type = t;
            size = s;
        }
    }

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
    private Buffer pending;

    public ClientConnection(String socketAddress)
    {
        this.cxnId = socketAddress;
        this.state = NewConnection;
        this.connectionTime = System.currentTimeMillis();
        this.pending = Buffer.buffer();
    }

    public void setChallenge(byte[] chal)
    {
        this.challenge = chal;
        this.state = ChallengeSent;
    }

    public MessageHeader readMessageHeader()
    {
        int msgType = pending.getUnsignedShort(0);
        int msgSize = pending.getUnsignedShort(2);
        return new MessageHeader(msgType, msgSize);
    }

    private void readRawSocketMessage()
    {
        // While I can read the header... (header is 4 bytes)
        while(pending.length() >= HEADER_LEN) {
            MessageHeader mh = null;
            if(pending.length() >= HEADER_LEN) {
                mh = readMessageHeader();
                if (mh.size > pending.length() - HEADER_LEN)
                    return;
                JsonObject contents = readMessageContents(mh);
                publishParsedMessage(contents);
                pending = pending.slice(HEADER_LEN + mh.size, pending.length());
            }
        }

        pending = pending.copy();
    }

    private JsonObject readMessageContents(MessageHeader hdr)
    {
        String msg = pending.getString(HEADER_LEN, hdr.size + (HEADER_LEN - 1));
        if(msg.length() != hdr.size - 1) // We subtract 1 from the hdr size because the trailing null counts against us.
            L.warn("Unexpected message length! Expected: {}, got {}, buflen {}, msg: {}", hdr.size, msg.length(),
                    pending.length(), msg);

        return new JsonObject(msg);
    }

    private void publishParsedMessage(JsonObject message)
    {
        JsonObject publishable = new JsonObject()
                .put("origin", cxnId)
                .put("payload", message);
        L.debug("Received {}", message.encode());
        Main.vertx.eventBus().publish(GameVerticle.EVENTBUS_ADDRESS, publishable);
    }

    public void handleMessage(Buffer buffer)
    {
        L.debug("Handling message of len {}", buffer.length());
        this.lastMessageTime = System.currentTimeMillis();
        pending.appendBuffer(buffer);
        L.debug("Pending buffer len {}", pending.length());
        readRawSocketMessage();
    }
}
