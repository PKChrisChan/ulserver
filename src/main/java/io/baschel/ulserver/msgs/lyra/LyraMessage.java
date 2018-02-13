package io.baschel.ulserver.msgs.lyra;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property="$type")
@JsonSubTypes({
    @JsonSubTypes.Type(value=LmArts.class, name="LmArts"),
    @JsonSubTypes.Type(value=LmItem.class, name="LmItem"),
    @JsonSubTypes.Type(value=LmItemHdr.class, name="LmItemHdr"),
    @JsonSubTypes.Type(value=LmAvatar.class, name="LmAvatar"),
    @JsonSubTypes.Type(value=LmStats.class, name="LmStats"),
    @JsonSubTypes.Type(value=GMsg_PreLogin.class, name="GMsg_PreLogin"),
    @JsonSubTypes.Type(value=GMsg_PreLoginAck.class, name="GMsg_PreLoginAck"),
    @JsonSubTypes.Type(value=GMsg_Login.class, name="GMsg_Login"),
    @JsonSubTypes.Type(value=GMsg_Ping.class, name="GMsg_Ping"),
    @JsonSubTypes.Type(value=GMsg_LoginAck.class, name="GMsg_LoginAck"),
    @JsonSubTypes.Type(value=RMsg_Speech.class, name="RMsg_Speech")
})
public interface LyraMessage {
}
