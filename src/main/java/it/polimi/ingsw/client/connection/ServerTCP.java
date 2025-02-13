package it.polimi.ingsw.client.connection;

import it.polimi.ingsw.client.connection.TCPThread.ServerTCP_IO;
import it.polimi.ingsw.client.model.ClientModel;
import it.polimi.ingsw.server.clientonserver.Client;
import it.polimi.ingsw.shared.*;
import it.polimi.ingsw.shared.model.Move;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.Map;


public class ServerTCP extends Server {
    private int id;
    private Socket serverSocket;

    private final ServerTCP_IO serverIO;

    public ServerTCP(IpAddressV4 ip, int port, ClientModel clientModel) throws ServerException {
        super(ip, port);
        try {
            serverSocket = new Socket();
            serverSocket.connect(new InetSocketAddress(ip.toString(), port), NetworkSettings.WaitingTimeMillis);
            serverIO = new ServerTCP_IO(serverSocket, clientModel);
        } catch (IOException e) {
            throw new ServerException(e.getMessage());
        }
    }
    /**
     * socket input buffer
     * @return the read line of the buffer
     */
    public MessageTcp in() throws RemoteException {
        return serverIO.in();

    }

    /**
     * send a message through socket connection
     * @param message is the message to send
     */
    public void out(String message){
        serverIO.out(message);
    }
    @Override
    public boolean login(Client client) throws ServerException {
        JSONObject content = new JSONObject();
        content.put("player", client.getPlayerName());
        MessageTcp login = new MessageTcp();
        login.setCommand(MessageTcp.MessageCommand.Login); //set command
        login.setContent(content);
        login.generateRequestID();
        out(login.toString());
        try {
            MessageTcp response = in(); //wait for response by server and create an object response
            while (!response.getCommand().equals(MessageTcp.MessageCommand.Login) && !response.getRequestID().equals(login.getRequestID())) //is there's more than one message in the buffer, then it read until he founds one suitable for the response
                response = in();
            return Boolean.parseBoolean(response.getContent().get("result").toString());
        }catch (RemoteException e){
            throw new ServerException("Error while connecting to server, " + e.getMessage());
        }
    }


    @Override
    public int getJoinedLobby(String playerName) throws ServerException {
        JSONObject content = new JSONObject();
        content.put("player", playerName);
        MessageTcp requestLobbies = new MessageTcp();
        requestLobbies.setCommand(MessageTcp.MessageCommand.GetJoinedLobby); //set command
        requestLobbies.setContent(content); //set playername as JsonObject
        requestLobbies.generateRequestID();
        out(requestLobbies.toString());
        try {
            MessageTcp response = in(); //wait for response by server and create an object response
            while (!response.getCommand().equals(MessageTcp.MessageCommand.GetJoinedLobby) && !response.getRequestID().equals(requestLobbies.getRequestID())) //is there's more than one message in the buffer, then it read until he founds one suitable for the response
                response = in();
            return (int) Long.parseLong(response.getContent().get("lobby").toString());
        }catch (RemoteException e){
            throw new ServerException(e.getMessage());
        }
    }
    @Override
    public Map<Integer, Integer> getAvailableLobbies()throws ServerException {
        MessageTcp requestLobbies = new MessageTcp();
        requestLobbies.setCommand(MessageTcp.MessageCommand.GetAvailableLobbies); //set command
        requestLobbies.generateRequestID();
        out(requestLobbies.toString());
        try {
            MessageTcp response = in(); //wait for response by server and create an object response
            while (!response.getCommand().equals(MessageTcp.MessageCommand.GetAvailableLobbies) && !response.getRequestID().equals(requestLobbies.getRequestID())) //is there's more than one message in the buffer, then it read until he founds one suitable for the response
                response = in();
            return Jsonable.json2mapInt((JSONObject) response.getContent().get("lobbies"));
        }catch (RemoteException e){
            throw new ServerException(e.getMessage());
        }
    }

    @Override
    public void joinRandomLobby(Client client) throws ServerException {
        MessageTcp requestLobbies = new MessageTcp();
        requestLobbies.setCommand(MessageTcp.MessageCommand.JoinRandomLobby); //set command
        requestLobbies.generateRequestID();
        out(requestLobbies.toString());
        try {
            MessageTcp response = in(); //wait for response by server and create an object response
            while (!response.getCommand().equals(MessageTcp.MessageCommand.JoinRandomLobby) && !response.getRequestID().equals(requestLobbies.getRequestID())) //is there's more than one message in the buffer, then it read until he founds one suitable for the response
                response = in();
            long Lobbyid = Long.parseLong(response.getContent().get("lobbyID").toString());
            if (Lobbyid > 0)
                this.id = (int)Lobbyid;
        }catch (RemoteException e){
            throw new ServerException(e.getMessage());
        }
    }
    @Override
    public void createLobby(Client client) throws ServerException {
        MessageTcp requestLobbies = new MessageTcp();
        requestLobbies.setCommand(MessageTcp.MessageCommand.CreateLobby); //set command
        requestLobbies.generateRequestID();
        out(requestLobbies.toString());
        try {
            MessageTcp response = in(); //wait for response by server and create an object response
            while (!response.getCommand().equals(MessageTcp.MessageCommand.CreateLobby) && !response.getRequestID().equals(requestLobbies.getRequestID()))//is there's more than one message in the buffer, then it read until he founds one suitable for the response
                response = in();
            long Lobbyid = Long.parseLong(response.getContent().get("lobbyID").toString());
            if (Lobbyid > 0)
                this.id = (int) Lobbyid;
        }catch (RemoteException e){
            throw new ServerException(e.getMessage());
        }
    }


    @Override
    public void joinSelectedLobby(Client client, int id)throws ServerException {
        JSONObject content = new JSONObject();
        content.put("lobbyID", id);
        MessageTcp requestLobbies = new MessageTcp();
        requestLobbies.setCommand(MessageTcp.MessageCommand.JoinSelectedLobby); //set command
        requestLobbies.setContent(content);
        requestLobbies.generateRequestID();
        out(requestLobbies.toString());
        try {
            MessageTcp response = in(); //wait for response by server and create an object response
            while (!response.getCommand().equals(MessageTcp.MessageCommand.JoinSelectedLobby) && !response.getRequestID().equals(requestLobbies.getRequestID())) //is there's more than one message in the buffer, then it read until he founds one suitable for the response
                response = in();
            long Lobbyid = Long.parseLong(response.getContent().get("lobbyID").toString());
            if (Lobbyid > 0)
                this.id = (int) Lobbyid;
            else
                throw new ServerException("Lobby join error");
        }catch (RemoteException e){
            throw new ServerException(e.getMessage());
        }
    }
    @Override
    public void postToLiveChat(String playerName, String message) throws LobbyException {
        ChatMessage chatMessage = new ChatMessage(playerName, message, Color.Black);
        JSONObject content = new JSONObject();
        MessageTcp postMessage = new MessageTcp();
        content.put("chat", chatMessage.toJson());
        postMessage.setCommand(MessageTcp.MessageCommand.PostToLiveChat); //set command
        postMessage.setContent(content); //set playername as JsonObject
        postMessage.generateRequestID();
        out(postMessage.toString());
        try {
            MessageTcp response = in(); //wait for response by server and create an object responses
            while (!response.getCommand().equals(MessageTcp.MessageCommand.PostToLiveChat) && !response.getRequestID().equals(postMessage.getRequestID())) //is there's more than one message in the buffer, then it read until he founds one suitable for the response
                response = in();
            boolean errorFound = Boolean.parseBoolean(response.getContent().get("errors").toString());
            if (errorFound) {
                throw new LobbyException("Error in Lobby");
            }
        }catch (RemoteException e){
            throw new LobbyException(e.getMessage());
        }
    }

    @Override
    public void postSecretToLiveChat(String sender, String receiver, String message) throws LobbyException{
        PrivateChatMessage chatMessage = new PrivateChatMessage(sender,receiver,message,Color.Black);
        JSONObject content = new JSONObject();
        content.put("chat", chatMessage.toJson());
        MessageTcp postMessage = new MessageTcp();
        postMessage.setCommand(MessageTcp.MessageCommand.PostSecretToLiveChat); //set command
        postMessage.setContent(content); //set playername as JsonObject
        postMessage.generateRequestID();
        out(postMessage.toString());
        try {
            MessageTcp response = in(); //wait for response by server and create an object responses
            while (!response.getCommand().equals(MessageTcp.MessageCommand.PostSecretToLiveChat) && !response.getRequestID().equals(postMessage.getRequestID())) //is there's more than one message in the buffer, then it read until he founds one suitable for the response
                response = in();
            boolean errorFound = Boolean.parseBoolean(response.getContent().get("errors").toString());
            if (errorFound) {
                throw new LobbyException("Error in Lobby");
            }
        }catch (RemoteException e){
            throw new LobbyException(e.getMessage());
        }
    }

    @Override
    public void quitGame(String player) throws LobbyException{
        MessageTcp quitMessage = new MessageTcp();
        quitMessage.setCommand(MessageTcp.MessageCommand.Quit); //set command
        quitMessage.generateRequestID();
        out(quitMessage.toString());

        serverIO.terminate();

    }

    @Override
    public boolean matchHasStarted() throws LobbyException {
        MessageTcp hasStartedMessage = new MessageTcp();
        hasStartedMessage.setCommand(MessageTcp.MessageCommand.MatchHasStarted); //set command
        hasStartedMessage.generateRequestID();
        out(hasStartedMessage.toString());
        try {
            MessageTcp response = in(); //wait for response by server and create an object responses
            while (!response.getCommand().equals(MessageTcp.MessageCommand.MatchHasStarted) && !response.getRequestID().equals(hasStartedMessage.getRequestID())) //is there's more than one message in the buffer, then it read until he founds one suitable for the response
                response = in();
            return Boolean.parseBoolean(response.getContent().get("started").toString());
        }catch (RemoteException e){
            throw new LobbyException(e.getMessage());
        }
    }

    @Override
    public void postMove(String player, Move move) throws LobbyException {
        JSONObject content = new JSONObject();
        content.put("move",move.toJson());
        MessageTcp postMoveMessage = new MessageTcp();
        postMoveMessage.setCommand(MessageTcp.MessageCommand.PostMove); //set command
        postMoveMessage.setContent(content);
        postMoveMessage.generateRequestID();
        out(postMoveMessage.toString());
        try {
            MessageTcp response = in(); //wait for response by server and create an object responses
            while (!response.getCommand().equals(MessageTcp.MessageCommand.PostMove) && !response.getRequestID().equals(postMoveMessage.getRequestID())) //is there's more than one message in the buffer, then it read until he founds one suitable for the response
                response = in();
            boolean errorFound = Boolean.parseBoolean(response.getContent().get("errors").toString());
            if (errorFound) {
                throw new LobbyException("Error in Lobby");
            }
        }catch (RemoteException e){
            throw new LobbyException(e.getMessage());
        }

    }

    @Override
    public boolean startGame(String player, boolean erasePreviousMatches)throws LobbyException {
        JSONObject content = new JSONObject();
        content.put("erasePreviousMatches", erasePreviousMatches);
        MessageTcp startGame = new MessageTcp();
        startGame.setCommand(MessageTcp.MessageCommand.StartGame); //set command
        startGame.setContent(content); //set playername as JsonObject
        startGame.generateRequestID();
        out(startGame.toString());
        try {
            MessageTcp response = in(); //wait for response by server and create an object response
            while (!response.getCommand().equals(MessageTcp.MessageCommand.StartGame) && !response.getRequestID().equals(startGame.getRequestID())) //is there's more than one message in the buffer, then it read until he founds one suitable for the response
                response = in();
            return Boolean.parseBoolean(response.getContent().get("start").toString());
        }catch (RemoteException e){
            throw new LobbyException(e.getMessage());
        }
    }
    @Override
    public boolean isLobbyAdmin(String player)throws LobbyException {
        JSONObject content = new JSONObject();
        content.put("player", player);
        MessageTcp isAdmin = new MessageTcp();
        isAdmin.setCommand(MessageTcp.MessageCommand.IsLobbyAdmin); //set command
        isAdmin.setContent(content); //set playername as JsonObject
        isAdmin.generateRequestID();
        out(isAdmin.toString());
        try {
            MessageTcp response = in(); //wait for response by server and create an object response
            while (!response.getCommand().equals(MessageTcp.MessageCommand.IsLobbyAdmin) && !response.getRequestID().equals(isAdmin.getRequestID())) //is there's more than one message in the buffer, then it read until he founds one suitable for the response
                response = in();
            return Boolean.parseBoolean(response.getContent().get("admin").toString());
        }catch (RemoteException e){
            throw new LobbyException(e.getMessage());
        }
    }
    @Override
    public int getLobbyID()throws LobbyException {
        if(id >0)
            return this.id;
        else
            throw new LobbyException("lobby doesn't exists");
    }

    /**
     * This method is used to observe the player supposed
     * to play in the current turn.
     *
     * @return String name of the player
     */
    @Override
    public String getCurrentPlayer() throws LobbyException {
        MessageTcp getPlayer = new MessageTcp();
        getPlayer.setCommand(MessageTcp.MessageCommand.GetCurrentPlayer); //set command
        getPlayer.generateRequestID();
        out(getPlayer.toString());
        try {
            MessageTcp response = in(); //wait for response by server and create an object response
            while (!response.getCommand().equals(MessageTcp.MessageCommand.GetCurrentPlayer) && !response.getRequestID().equals(getPlayer.getRequestID())) //is there's more than one message in the buffer, then it read until he founds one suitable for the response
                response = in();
            return response.getContent().get("player").toString();
        }catch (RemoteException | NullPointerException e){
            throw new LobbyException(e.getMessage());
        }
    }

    /**
     * This function is used to check if the client was already connected to
     * a lobby and was previously disconnected.
     *
     * @param playerName String name of the player
     * @return Int id of the lobby if the player was previously connected,
     * -1 if not.
     */
    @Override
    public int disconnectedFromLobby(String playerName) throws ServerException {
        MessageTcp disconnectedLobby = new MessageTcp();
        JSONObject content = new JSONObject();
        content.put("player", playerName);
        disconnectedLobby.setCommand(MessageTcp.MessageCommand.DisconnectedFromLobby); //set command
        disconnectedLobby.generateRequestID();
        disconnectedLobby.setContent(content);
        out(disconnectedLobby.toString());
        try {
            MessageTcp response = in(); //wait for response by server and create an object response
            while (!response.getCommand().equals(MessageTcp.MessageCommand.DisconnectedFromLobby) && !response.getRequestID().equals(disconnectedLobby.getRequestID())) //is there's more than one message in the buffer, then it read until he founds one suitable for the response
                response = in();
            return Integer.parseInt(response.getContent().get("lobby").toString());
        }catch (RemoteException | NullPointerException e){
            throw new ServerException(e.getMessage());
        }
    }
}
