package io.baschel.ulserver.net;

import io.baschel.ulserver.game.GameVerticle;
import io.baschel.ulserver.game.RoomVerticle;
import io.baschel.ulserver.msgs.MessageUtils;
import io.baschel.ulserver.msgs.internal.DisconnectClient;
import io.baschel.ulserver.msgs.lyra.RMsg_Update;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import static io.baschel.ulserver.Main.vertx;

public class ClientConnection {
    public static final int HEADER_LEN = 4;
    private static Logger L = LoggerFactory.getLogger(ClientConnection.class);

    public void onClose(Void v) {
        L.debug("Received close for connection {}", cxnId);
        new DisconnectClient(cxnId).publish();
    }

    public static class MessageHeader {
        public int type;
        public int size;

        MessageHeader(int t, int s) {
            type = t;
            size = s;
        }
    }

    private long connectionTime;
    private long lastMessageTime;
    private String cxnId;
    private byte[] challenge;
    private Buffer pending;

    public ClientConnection(String socketAddress) {
        this.cxnId = socketAddress;
        this.connectionTime = System.currentTimeMillis();
        this.pending = Buffer.buffer();
    }

    public MessageHeader readMessageHeader() {
        int msgType = pending.getUnsignedShort(0);
        int msgSize = pending.getUnsignedShort(2);
        return new MessageHeader(msgType, msgSize);
    }

    private void readRawSocketMessage() {
        // While I can read the header... (header is 4 bytes)
        while (pending.length() >= HEADER_LEN) {
            MessageHeader mh = null;
            if (pending.length() >= HEADER_LEN) {
                mh = readMessageHeader();
                if (mh.size > pending.length() - HEADER_LEN)
                    return;
                if (mh.type != MessageUtils.JSON_TYPE) {
                    Buffer binaryContents = readRawMessageBytes(mh);
                    if (mh.type == RMsg_Update.MSG_TYPE)
                        publishPositionUpdate(binaryContents);
                } else {
                    JsonObject contents = readMessageContents(mh);
                    publishParsedMessage(contents);
                }
                pending = pending.slice(HEADER_LEN + mh.size, pending.length()).copy();
            }
        }

        pending = pending.copy();
    }

    private Buffer readRawMessageBytes(MessageHeader mh) {
        Buffer b = pending.getBuffer(HEADER_LEN, mh.size + HEADER_LEN);
        if (b.length() != mh.size)
            L.warn("Unexpected message length! Expected: {} got {}, msgtype: {}",
                    mh.size, b.length(), mh.type);
        return b;
    }

    private void publishPositionUpdate(Buffer buf) {
        RMsg_Update up = new RMsg_Update();
        up.initFromBinary(buf);
        vertx.eventBus().send(RoomVerticle.eventBusAddressPosUpdate(up.levelid, up.roomid), buf);
    }

    private JsonObject readMessageContents(MessageHeader hdr) {
        String msg = pending.getString(HEADER_LEN, hdr.size + (HEADER_LEN - 1));
        if (msg.length() != hdr.size - 1) // We subtract 1 from the hdr size because the trailing null counts against us.
            L.warn("Unexpected message length! Expected: {}, got {}, buflen {}, msg: {}", hdr.size, msg.length(),
                    pending.length(), msg);

        return new JsonObject(msg);
    }

    private void publishParsedMessage(JsonObject message) {
        JsonObject publishable = new JsonObject()
                .put("origin", cxnId)
                .put("payload", message);
        L.debug("Received {}", message.encode());
        vertx.eventBus().publish(GameVerticle.EVENTBUS_ADDRESS, publishable);
    }

    public void handleMessage(Buffer buffer) {
        this.lastMessageTime = System.currentTimeMillis();
        pending.appendBuffer(buffer);
        readRawSocketMessage();
    }
}
