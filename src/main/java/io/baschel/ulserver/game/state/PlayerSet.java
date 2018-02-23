package io.baschel.ulserver.game.state;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class PlayerSet {
    private Set<AbstractPlayerRecord> loggedInPlayers = new HashSet<>();
    private Map<String, Member> memberMap = new HashMap<>();
    // Indices
    private Map<String, Map<Object, Set<AbstractPlayerRecord>>> playerRecordIndices = new HashMap<>();
    private static final Logger L = LoggerFactory.getLogger(PlayerSet.class);
    public void addIndex(String field)
    {
        if(playerRecordIndices.containsKey(field))
            return;
        Map<Object, Set<AbstractPlayerRecord>> indexMap = new HashMap<>();
        playerRecordIndices.put(field, indexMap);
        doIndex(field);
    }

    public Set<AbstractPlayerRecord> getPlayers(String field, Object value)
    {
        Map<Object, Set<AbstractPlayerRecord>> index = playerRecordIndices.get(field);
        if(index == null)
            throw new RuntimeException("No index created for field " + field);

        Set<AbstractPlayerRecord> entries = index.getOrDefault(value, new HashSet<>());
        return entries;
    }

    private Member getMember(String field, Class<? extends AbstractPlayerRecord> clazz)
    {
        return memberMap.computeIfAbsent(field, f -> {
            try {
                return clazz.getField(f);
            } catch (NoSuchFieldException e) {
                try {
                    return clazz.getMethod(f);
                } catch (NoSuchMethodException e1) {
                    L.error("Failed to find field or method named {} on PlayerRecord!", e1, f);
                    return null;
                }
            }
        });
    }

    private Function<AbstractPlayerRecord, Object> getMemberValue(String fieldOrMethod, Class<? extends AbstractPlayerRecord> clazz)
    {
        Member mem = getMember(fieldOrMethod, clazz);
        if(mem instanceof Field)
        {
            Field f = (Field)mem;
            return p -> {
                try {
                    return f.get(p);
                } catch (IllegalAccessException e) {
                    L.error("Failed to get {} from {}", e, fieldOrMethod, p.pid);
                }
                return null;
            };
        }
        else if(mem instanceof Method)
        {
            Method meth = (Method)mem;
            return p -> {
                try {
                    return meth.invoke(p);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    L.error("Failed to invoke {} on {}", e, fieldOrMethod, p.pid);
                }
                return null;
            };
        }

        return p->null;
    }

    private void indexRecord(AbstractPlayerRecord p, String field) {
        Object o = getMemberValue(field, p.getClass()).apply(p);
        if(o == null) {
            L.error("Not indexing {}", p.pid);
            return;
        }
        playerRecordIndices.get(field).computeIfAbsent(o, k -> new HashSet<>());
        playerRecordIndices.get(field).get(o).add(p);
    }

    private void removeRecord(AbstractPlayerRecord p, String field) {
        Object o = getMemberValue(field, p.getClass()).apply(p);
        removeRecord(p, field, o);
    }

    private void removeRecord(AbstractPlayerRecord p, String field, Object o)
    {
        if(o == null) {
            L.error("Can't get memberValue for {} not removing {} from index", field, p.pid);
            return;
        }

        Set<AbstractPlayerRecord> playerSet = playerRecordIndices.get(field).get(o);
        if(playerSet != null) {
            playerSet.remove(p);
            // TODO MDA: Necessary?
            if (playerSet.size() == 0)
                playerRecordIndices.get(field).remove(o);
        } else {
            L.error("Attempting to locate {} for {} - not found. Got {}", field, p.pid, o);
        }
    }

    private void indexRecord(AbstractPlayerRecord p)
    {
        playerRecordIndices.keySet().forEach(k -> indexRecord(p, k));
    }

    private void doIndex(String field)
    {
        loggedInPlayers.forEach(p -> indexRecord(p, field));
    }

    public void addPlayer(AbstractPlayerRecord rec)
    {
        if(loggedInPlayers.contains(rec))
            return;
        loggedInPlayers.add(rec);
        indexRecord(rec);
    }

    public void removePlayer(AbstractPlayerRecord rec)
    {
        if(!loggedInPlayers.remove(rec))
            return;
        playerRecordIndices.keySet().forEach(key -> {
            removeRecord(rec, key);
        });
    }

    public void reindexPlayer(AbstractPlayerRecord rec)
    {
        removePlayer(rec);
        addPlayer(rec);
    }

    public void reindexSingleField(String field, Object oldVal, AbstractPlayerRecord rec)
    {
        Object newVal = getMemberValue(field, rec.getClass()).apply(rec);
        if(newVal == oldVal)
            return;
        removeRecord(rec, field, oldVal);
        indexRecord(rec, field);
    }

    public Set<AbstractPlayerRecord> getAllPlayers()
    {
        return loggedInPlayers;
    }
}
