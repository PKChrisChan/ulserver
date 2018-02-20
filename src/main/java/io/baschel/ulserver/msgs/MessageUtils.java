package io.baschel.ulserver.msgs;

import io.baschel.ulserver.Main;
import io.baschel.ulserver.msgs.lyra.LyraMessage;
import io.baschel.ulserver.util.Json;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.io.UnsupportedEncodingException;

public class MessageUtils {
    private static Logger L = LoggerFactory.getLogger(MessageUtils.class);
    public static final int JSON_TYPE = 2057;

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
        L.debug("Sending message {} to {}", obj.encode(), source);
        Buffer buf = Buffer.buffer();
        buf.appendUnsignedShort(JSON_TYPE);
        String json = obj.encode();
        try {
            buf.appendUnsignedShort(json.getBytes("UTF-8").length + 1); // trailing null!
            buf.appendBytes(json.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        byte b = 0;
        buf.appendByte(b);
        Main.vertx.eventBus().send(source, buf);
    }

    public static void sendRawMessage(String source, LyraMessage message, int type)
    {
        Buffer msg = Buffer.buffer();
        Buffer content = message.asBinary();
        msg.appendUnsignedShort(type);
        msg.appendUnsignedShort(content.length());
        msg.appendBuffer(content);
        Main.vertx.eventBus().send(source, msg);
    }
}
