package it.polimi.ingsw.server;

import it.polimi.ingsw.shared.*;
import it.polimi.ingsw.server.clientonserver.Client;
import it.polimi.ingsw.shared.RemoteInterfaces.ServerLobbyInterface;
import org.json.simple.JSONObject;

import java.rmi.RemoteException;
import java.util.*;
import java.util.stream.Collectors;

public class Lobby implements ServerLobbyInterface {
    private final int id;
    private final List<Client> clients = new ArrayList<>();
    private boolean ready = false;
    private boolean started = false;
    private boolean full = false;
    private final Chat chat;
    private Controller controller;

    public Lobby(Client firstPlayer, int id){
        this.clients.add(firstPlayer);
        this.id = id;
        this.chat = new Chat();
        chat.addPlayer(firstPlayer);
    }

    /**
     * add a player to lobby
     * @param client is the player object to add to the lobby
     */
    public void addPlayer(Client client){
        if(clients.contains(client)) //if player logged in previously
            return;
        if (clients.size() < GameSettings.maxSupportedPlayers) { //checks lobby isn't already full
            clients.add(client);
            chat.addPlayer(client);
        }else
            throw new RuntimeException("Lobby already full");

        //if the lobby has enough players it's ready to start
        if(clients.size() >= GameSettings.minSupportedPlayers)
            ready = true;
        if(clients.size() >= GameSettings.maxSupportedPlayers)
            full = true;
    }

    /**
     * @return true is the lobby is ready to start
     */
    public boolean isReady(){
        return ready;
    }
    /**
     * @return true is the lobby is full of players for it's capacity
     */
    public boolean isFull(){
        return full;
    }
    /**
     * @return list of players in this lobby
     */
    public ArrayList<Client> getClients(){
        return new ArrayList<>(clients); //TODO this is by reference
    }
    public List<String> getPlayerNames(){
        return clients.stream().
                map(Client::getPlayerName).
                collect(Collectors.toList());
    }
    /**
     * @return every message in that lobby
     */
    public Chat getChat(){
        return new Chat(chat);
    }

    /**
     * @return true if no players are in lobby
     */
    public boolean isEmpty(){
        return clients.size() == 0;
    }

    /**
     * Tells who the lobby admin is
     * @return the name of the lobby admin
     * @throws Exception when the lobby is empty
     */
    public String getLobbyAdmin() throws Exception {
        if(clients.size() == 0){
            throw new Exception("No Players");
        }
        else{
            return clients.get(0).getPlayerName();
        }
    }

    /**
     * @return true if match has startd
     */
    @Override
    public boolean matchHasStarted(){
        return started;
    }
    /**
     * start the lobby if it is ready and the player who has asked is admin
     * @return true if successful
     */
    @Override
    public boolean startGame(String player){
        try {
            if(!ready  || !getLobbyAdmin().equals(player))
                return false;
        }
        catch (Exception e){
            return false;
        }

        started = true;
        controller = new Controller(clients);

        final List<String> players = clients.stream().map(Client::getPlayerName).collect(Collectors.toList());
        for(Client c : clients)
            c.gameStarted(new LinkedList<>(players));

        System.out.println("MATCH STARTED IN LOBBY #"+id);
        return true;
    }

    /**
     * @param playerName
     * @return true if playerName is the name of the lobby admin
     * @throws RemoteException
     */
    @Override
    public boolean isLobbyAdmin(String playerName) throws RemoteException {
        if(isEmpty()){
            return false;
        }
        else{
            return clients.get(0).getPlayerName().equals(playerName);
        }
    }

    /**
     *
     * @param playerName is the player that is sending a message
     * @param message is the content
     * @throws Exception when message format is wrong
     */
    @Override
    public void postToLiveChat(String playerName, String message) throws Exception {
        if(playerName == null || message == null){
            throw new Exception("Wrong format of message");
        }
        chat.addMessage(playerName,message);
        for (Client client : clients) {
            client.postChatMessage(playerName, message);
        }
    }

    /**
     *
     * @param sender is the player that is sending a message
     * @param receiver is the player that is sending a message
     * @param message is the content
     * @throws Exception when message format is wrong
     */
    @Override
    public void postSecretToLiveChat(String sender, String receiver, String message) throws Exception {
        if(sender == null || receiver == null || message == null){
            throw new Exception("Wrong format of message");
        }
        chat.addSecret(sender,receiver,message);
    }

    @Override
    public void quitGame(String player) throws RemoteException {
        //TODO
    }

    @Override
    public void postMove(String player, JSONObject moveJson) throws RemoteException {
        Client playerInput = null;
        final Move move = new Move(moveJson);
        try{
            playerInput = clients.stream().filter(x -> x.getPlayerName().equals(player)).findFirst().orElse(null);
            if(playerInput != null) {
                controller.moveTiles(player, move);
            }
        } catch (ControllerGenericException e){
            if(playerInput != null)
                playerInput.postChatMessage("Server", e.getMessage());
        }
    }

    @Override
    public int getID(){
        return this.id;
    }
}
