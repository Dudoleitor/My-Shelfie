package it.polimi.ingsw.RMI;

import it.polimi.ingsw.shared.Client;
import it.polimi.ingsw.shared.ClientRMI;
import it.polimi.ingsw.shared.Constants;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.stream.Collectors;

public class ServerMain implements ServerRemoteInterface {
    private static boolean keepOn = true;
    private static final int port = Constants.port;

    /**
     * This list contains the clients connected to the
     * server that did not join a lobby yet.
     */
    private final List<Client> clientsWithoutLobby = new ArrayList<>();

    private final List<Lobby> lobbies = new ArrayList<>();

    private static Registry registry = null;
    private static InputSanitizer inputSanitizer;
    public static void main(String argv[]){
        ServerMain obj = new ServerMain();
        ServerRemoteInterface stub;
        try {
            stub = (ServerRemoteInterface) UnicastRemoteObject.exportObject(obj, port); //create an interface to export
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        try {
            registry = LocateRegistry.createRegistry(port); //create a registry that accepts request on a defined port
        } catch (RemoteException e) {
            e.printStackTrace(); //TODO to handle correctly
        }
        try {
            registry.bind("interface", stub); //Binds a remote reference to the specified name in this registry
        } catch (RemoteException e) { //TODO to handle correctly
            throw new RuntimeException(e);
        } catch (AlreadyBoundException e) {
            throw new RuntimeException(e);
        }


        System.out.println("Server is on");
        while (keepOn) {
            Thread.onSpinWait(); //is used to suspend the process and make it wait
        } //to keep it online
        System.out.println("Server is shutting down :D, don't forget to save... oh no too late");
        System.exit(0); //to shut down the server, maybe it doesn't shut down spontaneously because fo the interface it gave away
    }

    /**
     *
     * @param clientRMI is ths object used as RMI interface
     * @return true is login is successful
     * @throws RemoteException is there are problems with connection
     */
    @Override
    public boolean login(ClientRMI clientRMI) throws RemoteException {

        clientsWithoutLobby.add(clientRMI);
        System.out.println(clientRMI.getPlayerName() + " has just logged in");

        return true;
    }

    /**
     *
     * @param nick is the player name
     * @return list of lobby id of matches joined by the player
     */
    @Override
    public List<Integer> getJoinedLobbies(String nick){
        List<Integer> listLobbies = //get all lobbies already joined by the client
                lobbies.stream()
                        .filter(x -> x.getPlayers().contains(nick))
                        .map(Lobby::getId)
                        .collect(Collectors.toList());
        return listLobbies;
    }

    /**
     * @param client requesting to join the lobby
     * @return id of assigned lobby
     */
    @Override
    public int joinRandomLobby(Client client){ //TODO to handle a re-join of the same player possibility
        int lobbyID;
        Lobby lobby = lobbies.stream()
                    .filter(l -> !l.isReady()) //keep only not full lobbies
                    .findFirst() //find first lobby matched
                    .orElse(null);
        if(lobby != null){ //if a lobby exists then add player
            lobby.addPlayer(client); //if exists then add player
            lobbyID = lobby.getId();
        }else {
            lobbyID = createLobby(client); //otherwise creates new lobby
        }

        return lobbyID;
    }

    /**
     * @param client requesting to join the lobby
     */
    @Override
    public boolean joinSelectedLobby(Client client, int id){ //TODO to handle a re-join of the same player possibility
        Lobby lobby = lobbies.stream()
                .filter(x -> x.getId() == id) //verify lobby exists and is not full
                .findFirst().orElse(null);
        if(lobby == null) //if a lobby exists then add player
            return false;
        try {
            lobby.addPlayer(client); //if exists then add player
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }
    /**
     * @param client requesting to create the lobby
     */
    @Override
    public int createLobby(Client client){
        int nextFreeKey = lobbies.stream().map(Lobby::getId).max(Integer::compareTo).orElse(0) + 1; //find max allocated key and gives back next one
        Lobby lobby = new Lobby(client, nextFreeKey);
        try {
            LobbyRemoteInterface lobbyStub = (LobbyRemoteInterface) UnicastRemoteObject.exportObject(lobby, port); //create an interface to export
            registry.bind(String.valueOf(lobby.getId()), lobbyStub); //Binds a remote reference to the specified name in this registry

        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } catch (AlreadyBoundException e) {
            throw new RuntimeException(e);
        }
        lobbies.add(lobby);
        return lobby.getId();
    }

    @Override
    public Map<Integer, Integer> showAvailableLobbbies() throws RemoteException {
        Map<Integer, Integer> lobbyMap = new HashMap<>();
        lobbies.stream()
                .filter(x -> !x.hasStarted())
                .forEach(x -> lobbyMap.put(x.getId(), x.getPlayers().size())); //add id lobby + num of players currently in
        return lobbyMap;
    }


}
