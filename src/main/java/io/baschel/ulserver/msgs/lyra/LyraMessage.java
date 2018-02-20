package io.baschel.ulserver.msgs.lyra;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.vertx.core.buffer.Buffer;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property="$type")
public interface LyraMessage {

    default LyraMessage initFromBinary(Buffer buf)
    {
        return this;
    }

    default Buffer asBinary()
    {
        return null;
    }
}
