package it.polimi.ingsw.RMI;
import it.polimi.ingsw.server.clientonserver.Client;
import it.polimi.ingsw.server.clientonserver.ClientRMI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.List;
import java.util.Map;

public interface ServerRemoteInterface extends Remote {
    boolean login(ClientRMI clientRMI) throws RemoteException;
    List<Integer> getJoinedLobbies(String nick) throws RemoteException;
    LobbyRemoteInterface joinRandomLobby(Client client) throws RemoteException;
    LobbyRemoteInterface createLobby(Client client) throws RemoteException;
    Map<Integer,Integer> showAvailableLobbbies() throws RemoteException;
    LobbyRemoteInterface joinSelectedLobby(Client client, int id) throws RemoteException;

}
