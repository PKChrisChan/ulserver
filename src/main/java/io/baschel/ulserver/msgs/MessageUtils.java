package io.baschel.ulserver.msgs;

import io.baschel.ulserver.Main;
import io.baschel.ulserver.msgs.lyra.LyraMessage;
import io.baschel.ulserver.util.Json;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class MessageUtils {
    private static Logger L = LoggerFactory.getLogger(MessageUtils.class);
    public static final int JSON_TYPE = 2057;

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

    public static JsonObject readMessageContents(MessageHeader hdr, Buffer buf)
    {
        String msg = buf.getString(4, buf.length() - 1);
        if(msg.length() != hdr.size)
            L.warn("Unexpected message length! Expected: {0}, got {1}, buflen {2}, msg: {3}", hdr.size, msg.length(), buf.length(), msg);
        return new JsonObject(msg);
    }

    public static MessageHeader readMessageHeader(Buffer buf)
    {
        int msgType = buf.getUnsignedShort(0);
        int msgSize = buf.getUnsignedShort(2);
        return new MessageHeader(msgType, msgSize);
    }

    public static JsonObject readRawSocketMessage(Buffer buf)
    {
        MessageHeader mh = readMessageHeader(buf);
        return readMessageContents(mh, buf);
    }

    public static LyraMessage readSocketMessage(Buffer buf)
    {
        int msgType = buf.getUnsignedShort(0);
        int msgSize = buf.getUnsignedShort(2);
        String jsMsg = buf.getString(4, buf.length() - 1);
        return Json.objectFromJsonObject(new JsonObject(jsMsg), LyraMessage.class);
    }

    public static void sendJsonMessage(String source, LyraMessage message)
    {
        JsonObject obj = Json.objectToJsonObject(message);
        Buffer buf = Buffer.buffer();
        buf.appendUnsignedShort(JSON_TYPE);
        String json = obj.encode();
        buf.appendUnsignedShort(json.length());
        buf.appendString(json);
        byte b = 0;
        buf.appendByte(b);
        Main.vertx.eventBus().send(source, buf);
    }
}
