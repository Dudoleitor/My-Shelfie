package it.polimi.ingsw.server.clientonserver;

import it.polimi.ingsw.shared.Chat;
import it.polimi.ingsw.shared.RemoteInterfaces.ClientRemote;
import it.polimi.ingsw.shared.model.Tile;
import org.json.simple.JSONObject;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Objects;

/**
 * This object is used to send updates to a specific client.
 * A copy of this object will reside on the server.
 * It is a wrapper for ClientRemoteObject: it's
 * needed to handle RemoteExceptions.
 */
public class ClientRMI implements Client, Serializable {
    private final String playerName;
    private final ClientRemote clientRemote;
    private Chat chat;

    public ClientRMI(ClientRemote clientRemote) throws RemoteException {
        this.playerName = clientRemote.getPlayerName();
        this.clientRemote = clientRemote;
    }

    /**
     * This method is used to return the name of
     * the players using this client.
     * @return String, player's name.
     */
    public String getPlayerName() {return playerName;}

    /**
     * This method is used when a player picks a tile
     * from the board. It sends the message
     * to the remote view to remove the tile
     * from the board.
     *
     * @param position Position
     */
    @Override
    public void pickedFromBoard(JSONObject position) {
        try {
            clientRemote.pickedFromBoard(position);
        } catch (RemoteException e) {
            // TODO Handle exception
            e.printStackTrace();
        }
    }

    /**
     * This method is used to transfer the whole board
     * to the remote view,
     * it uses a json string.
     * @param board JSONObject.toJsonString
     */
    @Override
    public void refreshBoard(String board) {
        try {
            clientRemote.refreshBoard(board);
        } catch (RemoteException e) {
            // TODO Handle exception
            e.printStackTrace();
        }
    }

    /**
     * This method is used when a player inserts a single
     * tile into his shelf.
     * It is used to send the message to the remote view
     * of the client in order to insert the tile
     * into the shelf.
     * @param player String name of the player that moved the tile
     * @param column destination column of the shelf
     * @param tile Tile to insert
     */
    @Override
    public void putIntoShelf(String player, int column, Tile tile) {
        try {
            clientRemote.putIntoShelf(player, column, tile);
        } catch (RemoteException e) {
            // TODO Handle exception
            e.printStackTrace();
        }
    }

    /**
     * This method is used to transfer the whole shelf
     * of a player to the remote view,
     * it uses a json string.
     * @param player name of the player
     * @param shelf  JSONObject.toJsonString
     */
    @Override
    public void refreshShelf(String player, String shelf) {
        try {
            clientRemote.refreshShelf(player, shelf);
        } catch (RemoteException e) {
            // TODO Handle exception
            e.printStackTrace();
        }
    }

    /**
     * This method is used to send a chat message to clients.
     * @param sender Player's name
     * @param message String message
     */
    @Override
    public void postChatMessage(String sender, String message) {
        try {
            clientRemote.postChatMessage(sender, message);
        } catch (RemoteException e) {
            e.printStackTrace();
            // TODO Handle exception
        }
    }

    /**
     * This method is used to send the whole chat to the client,
     * it is used when a refresh is needed.
     * @param chat Chat object
     */
    @Override
    public void refreshChat(Chat chat) {

    }

    /**
     * This method is used when the lobby is ready and the
     * admin started the game.
     * @param players List of players, order is used to
     *                determine turns
     */
    @Override
    public void gameStarted(List<String> players) {
        try {
            clientRemote.gameStarted(players);
        } catch (RemoteException e) {
            e.printStackTrace();
            // TODO Handle exception
        }
    }

    /**
     * This function is used when the turn of a player ends.
     *
     * @param player Name of the player that will play next.
     */
    @Override
    public void updateTurn(String player) {
        try {
            clientRemote.nextTurn(player);
        } catch (RemoteException e) {
            e.printStackTrace();
            // TODO Handle exception
        }
    }

    /**
     * This method is used when a player achieves
     * a common goal and pops points from
     * its stack.
     * It is also used to init the common goal.
     *
     * @param id     ID of the common goal
     * @param points Copy of the stack with points that
     *               can still be achieved
     */
    @Override
    public void refreshCommonGoal(int id, List<Integer> points) {
       try {
           clientRemote.refreshCommonGoal(id, points);
       } catch (RemoteException e) {
           e.printStackTrace();
           // TODO Handle exception
       }
    }

    @Override
    public boolean equals(Object o) {  // Checking using LOWERCASE name
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientRMI clientRMI = (ClientRMI) o;
        return Objects.equals(playerName.toLowerCase(), clientRMI.playerName.toLowerCase());
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerName.toLowerCase());
    }
}
