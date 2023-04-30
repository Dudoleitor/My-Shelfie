package it.polimi.ingsw.server.clientonserver;

import it.polimi.ingsw.shared.Chat;
import it.polimi.ingsw.shared.model.Tile;
import org.json.simple.JSONObject;

import java.util.List;

/**
 * This object is used to send updates to
 * a specific client, it resides on the server.
 * ClientRMI and ClientTCP implement methods properly.
 */
public interface Client {

    /**
     * This method is used to return the name of
     * the players using this client.
     * @return String, player's name.
     */
    public String getPlayerName();

    /**
     * This method is used when a player picks a tile
     * from the board. It sends the message
     * to the remote view to remove the tile
     * from the board.
     *
     * @param position position
     */
    public void pickedFromBoard(JSONObject position);

    /**
     * This method is used to transfer the whole board
     * to the remote view,
     * it uses a json string.
     *
     * @param board JSONObject.toJsonString
     */
    public void refreshBoard(String board);

    /**
     * This method is used when a player inserts a single
     * tile into his shelf.
     * It is used to send the message to the remote view
     * of the client in order to insert the tile
     * into the shelf.
     *
     * @param player String name of the player that moved the tile
     * @param column destination column of the shelf
     * @param tile Tile to insert
     */
    public void putIntoShelf(String player, int column, Tile tile);

    /**
     * This method is used to transfer the whole shelf
     * of a player to the remote view of the client,
     * it uses a json string.
     *
     * @param player name of the player
     * @param shelf JSONObject.toJsonString
     */
    public void refreshShelf(String player, String shelf);

    /**
     * This method is used to send a chat message to clients.
     * @param sender Player's name
     * @param message String message
     */
    public void postChatMessage(String sender, String message);

    /**
     * This method is used to send the whole chat to the client,
     * it is used when a refresh is needed.
     * @param chat Chat object
     */
    public void refreshChat(Chat chat);

    /**
     * This method is used when the lobby is ready and the
     * admin started the game.
     * @param players List of players, order is used to
     *                determine turns
     */
    public void gameStarted(List<String> players);

    /**
     * This function is used when the turn of a player ends.
     * @param player Name of the player that will play next.
     */
    public void updateTurn(String player);

}
