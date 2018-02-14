package io.baschel.ulserver.msgs.internal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.baschel.ulserver.msgs.InternalServerMessage;
import io.baschel.ulserver.net.NetVerticle;

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
