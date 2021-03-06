package io.baschel.ulserver.game;

import io.baschel.ulserver.game.state.AbstractPlayerRecord;
import io.baschel.ulserver.game.state.GamePlayerRecord;
import io.baschel.ulserver.msgs.lyra.LyraMessage;

import java.util.Set;

public interface GameState {
    /**
     * Given an account id returns if any accts are logged in for it.
     */
    boolean isAccountLoggedIn(int acctId);

    GamePlayerRecord playerRecordForConnectionId(String connectionId);

    /**
     * Returns the GamePlayerRecord given the playerId or null if no playerRecord is found.
     * Note this does NOT query the database; it only checks in the GameState's memory.
     *
     * @param playerId of the logged in player
     * @return the record or null
     */
    GamePlayerRecord getPlayerRecord(int playerId);

    /**
     * Returns set of players in a room.
     *
     * @param locationId
     * @return
     */
    Set<AbstractPlayerRecord> roomPlayers(String locationId);

    /**
     * Sends a message to the specified pid. If player isn't present in our set of connections nothing is sent.
     *
     * @param message the message to send, in Lyra form.
     * @param record  the record of the player.
     */
    void sendToPlayer(LyraMessage message, AbstractPlayerRecord record);

    /**
     * Broadcasts a message to the room. The LevelRoomId is specified in L##R## format, e.g.
     * L20R09 for room 9 in level 20. Note it's ALWAYS TWO DIGITS!
     *
     * @param message
     * @param levelRoomId
     */
    void sendToRoom(LyraMessage message, String levelRoomId, AbstractPlayerRecord record);

    /**
     * Sends a message to the level. LevelID is specified in L## format, i.e. L20.
     *
     * @param message
     * @param levelId
     */
    void sendToLevel(LyraMessage message, String levelId);

    /**
     * Broadcasts a message to the game universe.
     *
     * @param message
     */
    void sendToGame(LyraMessage message);

    /**
     * Moves the player to a new level/room
     *
     * @param player
     * @param level
     * @param room
     */
    void movePlayer(GamePlayerRecord player, int level, int room);

    /**
     * Log player in.
     *
     * @param record
     */
    void login(AbstractPlayerRecord record);

    /**
     * log player out.
     *
     * @param record
     */
    void logout(AbstractPlayerRecord record);
}
