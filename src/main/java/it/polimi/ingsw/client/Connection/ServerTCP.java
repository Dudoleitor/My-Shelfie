package it.polimi.ingsw.client.Connection;

import it.polimi.ingsw.server.clientonserver.Client;
import it.polimi.ingsw.shared.IpAddressV4;
import it.polimi.ingsw.shared.Jsonable;
import it.polimi.ingsw.shared.MessageTcp;
import it.polimi.ingsw.shared.NetworkSettings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

public class ServerTCP extends Server {
    private Socket serverSocket;
    private PrintWriter serverOut;
    private BufferedReader serverIn;

    public ServerTCP(IpAddressV4 ip, int port) {
        super(ip, port);
        try {
            serverSocket = new Socket(ip.toString(), port);
            serverOut = new PrintWriter(serverSocket.getOutputStream(), true);
            serverIn = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String in(){
        boolean ready = false;
        try {
            while(!ready){
                if(serverIn.ready())
                    ready = true;
            }
            return serverIn.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * send a message through socket connection
     * @param message is the message to send
     */
    public void out(String message){
        serverOut.println(message);
    }
    @Override
    public boolean login(Client client) {
        MessageTcp login = new MessageTcp();
        login.setCommand(MessageTcp.MessageCommand.Login); //set command
        login.setContent(Jsonable.string2json(client.getPlayerName())); //set playername as JsonObject
        out(login.toString());
        MessageTcp response = new MessageTcp(in()); //wait for response by server and create an object response

        return Jsonable.json2boolean(response.getContent());


    }


    @Override
    public Map<Integer,Integer> getJoinedLobbies(String playerName) throws ServerException {
        MessageTcp requestLobbies = new MessageTcp();
        requestLobbies.setCommand(MessageTcp.MessageCommand.GetJoinedLobbies); //set command
        requestLobbies.setContent(Jsonable.string2json(playerName)); //set playername as JsonObject
        out(requestLobbies.toString());
        MessageTcp response = new MessageTcp(in()); //wait for response by server and create an object response

        return Jsonable.json2mapInt(response.getContent());
    }
    @Override
    public Map<Integer, Integer> getAvailableLobbies()throws ServerException {
        MessageTcp requestLobbies = new MessageTcp();
        requestLobbies.setCommand(MessageTcp.MessageCommand.GetAvailableLobbies); //set command
        out(requestLobbies.toString());
        MessageTcp response = new MessageTcp(in()); //wait for response by server and create an object response
        return Jsonable.json2mapInt(response.getContent());
    }

    @Override
    public Lobby joinRandomLobby(Client client) throws ServerException {
        MessageTcp requestLobbies = new MessageTcp();
        requestLobbies.setCommand(MessageTcp.MessageCommand.JoinRandomLobby); //set command
        out(requestLobbies.toString());
        MessageTcp response = new MessageTcp(in()); //wait for response by server and create an object response
        int lobbyID = Jsonable.json2int(response.getContent());
        if (lobbyID > 0)
            return new LobbyTCP(serverSocket, lobbyID); //create a local Lobby socket with server given port
        else
            return null;
    }

    @Override
    Lobby createLobby(Client client) throws ServerException {
        MessageTcp requestLobbies = new MessageTcp();
        requestLobbies.setCommand(MessageTcp.MessageCommand.CreateLobby); //set command
        out(requestLobbies.toString());
        MessageTcp response = new MessageTcp(in()); //wait for response by server and create an object response
        int lobbyID = Jsonable.json2int(response.getContent());
        if (lobbyID > 0)
            return new LobbyTCP(serverSocket, lobbyID); //create a local Lobby socket with server given port
        else
            return null;
    }


    @Override
    public Lobby joinSelectedLobby(Client client, int id)throws ServerException {
        MessageTcp requestLobbies = new MessageTcp();
        requestLobbies.setCommand(MessageTcp.MessageCommand.JoinSelectedLobby); //set command
        requestLobbies.setContent(Jsonable.int2json(id));
        out(requestLobbies.toString());
        MessageTcp response = new MessageTcp(in()); //wait for response by server and create an object response
        int lobbyID = Jsonable.json2int(response.getContent());
        if (lobbyID > 0)
            return new LobbyTCP(serverSocket, lobbyID); //create a local Lobby socket with server given port
        else
            return null;
    }
}
