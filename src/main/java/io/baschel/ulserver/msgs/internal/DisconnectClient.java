package io.baschel.ulserver.msgs.internal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.baschel.ulserver.msgs.InternalServerMessage;
import io.baschel.ulserver.net.NetVerticle;

/**
 * This is the message to use to force the socket to close.
 * You mut sent the actual socket address to close which is the GUID assigned
 * by VertX on client connection.
 *
 * If you want to disconnect a player, use DisconnectPlayer instead.
 */
public class DisconnectClient implements InternalServerMessage {
    private String clientId;

    @JsonCreator
    public DisconnectClient(@JsonProperty("clientId") String clientId)
    {
        this.clientId = clientId;
    }

    public String getClientId() { return clientId; }

    public String address() { return NetVerticle.EVENTBUS_ADDRESS; }
}
