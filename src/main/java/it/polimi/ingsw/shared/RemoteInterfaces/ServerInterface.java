package it.polimi.ingsw.shared.RemoteInterfaces;

import it.polimi.ingsw.server.clientonserver.Client;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

/**
 * This object is used to expose methods related to pre-lobby-join
 * operations, it resides on the server.
 * Methods are invoked using RMI by RMI clients, proper
 * adapters invoke methods when using TCP.
 */
public interface ServerInterface extends Remote {
    boolean login(Client client) throws RemoteException;
    ServerLobbyInterface joinRandomLobby(Client client) throws RemoteException;
    int getJoinedLobby(String nick) throws RemoteException;
    ServerLobbyInterface createLobby(Client client) throws RemoteException;
    Map<Integer,Integer> showAvailableLobbies() throws RemoteException;
    ServerLobbyInterface joinSelectedLobby(Client client, int id) throws RemoteException;
    int disconnectedFromLobby(String playerName) throws RemoteException;
    boolean disconnectClient(Client client) throws RemoteException;
}
