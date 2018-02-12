package io.baschel.ulserver.game;

import io.baschel.ulserver.msgs.lyra.LyraMessage;

public interface GameState {
    /**
     * Given an account id returns if any accts are logged in for it.
     */
    boolean isAccountLoggedIn(int acctId);

    /**
     * Returns the pid for the connection id if the player has completed logging in.
     * Returns null if no pid is found.
     * @param connectionId the socket address
     * @return the pid or -1 if not present
     */
    int playerIdForConnectionId(String connectionId);

    /**
     * Returns the PlayerRecord given the playerId or null if no playerRecord is found.
     * Note this does NOT query the database; it only checks in the GameState's memory.
     * @param playerId of the logged in player
     * @return the record or null
     */
    PlayerRecord getPlayerRecord(int playerId);

    /**
     * Sends a message to the specified pid. If player isn't present in our set of connections nothing is sent.
     * @param message the message to send, in Lyra form.
     * @param playerId the pid of the player.
     */
    void sendToPlayer(LyraMessage message, int playerId);

    /**
     * Broadcasts a message to the room. The LevelRoomId is specified in L##R## format, e.g.
     * L20R09 for room 9 in level 20. Note it's ALWAYS TWO DIGITS!
     * @param message
     * @param levelRoomId
     */
    void sendToRoom(LyraMessage message, String levelRoomId);

    /**
     * Sends a message to the level. LevelID is specified in L## format, i.e. L20.
     * @param message
     * @param levelId
     */
    void sendToLevel(LyraMessage message, String levelId);

    /**
     * Broadcasts a message to the game universe.
     * @param message
     */
    void sendToGame(LyraMessage message);
}
