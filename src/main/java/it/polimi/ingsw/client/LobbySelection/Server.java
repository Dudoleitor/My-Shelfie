package it.polimi.ingsw.client.LobbySelection;

import it.polimi.ingsw.client.Lobby.Lobby;
import it.polimi.ingsw.server.clientonserver.Client;
import it.polimi.ingsw.shared.IpAddressV4;

import java.util.Map;

public abstract class Server {
    IpAddressV4 ip;
    int port;
    Server(IpAddressV4 ip, int port){
        this.ip = ip;
        this.port = port;
    }

    /**
     * Login the client
     * @param client
     * @return true if login was successful
     */
    abstract boolean login(Client client);

    /**
     * Join the first available Lobby
     * @param client
     * @return the Lobby object that will handle the connection with
     * the joined lobby
     */
    abstract Lobby joinRandomLobby(Client client);

    /**
     * Create a lobby on server
     * @param client
     * @return the Lobby object that will handle the connection with
     * the joined lobby
     */
    abstract Lobby createLobby(Client client);

    /**
     * Get all the lobbies in which the player can log
     * @return a map of LobbyID - Number of Players in lobby
     */
    abstract Map<Integer,Integer> getAvailableLobbies();

    /**
     * Get all lobbies in which the client is present
     * @param playerName playerName of client
     * @return
     */
    abstract Map<Integer,Integer> getJoinedLobbies(String playerName);

    /**
     * Join a specific lobby
     * @param client
     * @param id
     * @return the Lobby object that will handle the connection with
     * the joined lobby
     */
    abstract Lobby joinSelectedLobby(Client client, int id);

    /**
     * Generate a client object that is compatible with the
     * connection type of server
     * @param playerName
     * @return the compatible client object
     */
    abstract Client generateClient(String playerName);
}
