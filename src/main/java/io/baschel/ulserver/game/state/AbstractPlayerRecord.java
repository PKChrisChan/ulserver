package io.baschel.ulserver.game.state;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.baschel.ulserver.msgs.lyra.LmAvatar;
import io.baschel.ulserver.msgs.lyra.LmPeerUpdate;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class AbstractPlayerRecord {
    public String connectionId;
    public int pid;
    public String name;
    // This is updated more frequently in Room!
    public LmPeerUpdate lastUpdate;
    public LmAvatar avatar;

    public String upperName() {
        return name.toUpperCase();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractPlayerRecord that = (AbstractPlayerRecord) o;

        if (pid != that.pid) return false;
        if (!upperName().equals(that.upperName()))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = connectionId != null ? connectionId.hashCode() : 0;
        result = 31 * result + pid;
        result = 31 * result + upperName().hashCode();
        return result;
    }
}
