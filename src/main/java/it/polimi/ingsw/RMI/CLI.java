package it.polimi.ingsw.RMI;

import it.polimi.ingsw.server.Move;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class CLI extends Thread{
    private String playerName;
    private LobbyRemoteInterface stub;
    private static Concurrent_cli_IO io = new Concurrent_cli_IO(new ReentrantLock());
    private InputSanitizer inputSanitizer = new InputSanitizer();
    private boolean exit = false;
    private Chat chat;

    CLI(String playerName, LobbyRemoteInterface stub){
        this.playerName = playerName;
        this.stub = stub;
        this.chat = new Chat();
    }
    protected void loopCommands(){
        String command;
        while(!exit){ //Receive commands until "exit" command is launched
            try{
                command = io.scan();
                //Invalid command
                if(!inputSanitizer.isValidMessage(command)){
                    io.printErrorMessage("Invalid format");
                }
                else{
                    Command toExecute = Command.stringToCommand(command);
                    executeUserCommand(toExecute);
                }
            }
            catch (RemoteException e){
                io.printErrorMessage("Error in RMI");
            } catch (Exception e) {
                e.printStackTrace();
                io.printErrorMessage("Error in Message Format");
            }
        }
    }
    protected void executeUserCommand(Command com) throws Exception {
        //execute action for every command
        switch (com){
            case Exit: //quit game
                io.printMessage("You quit");
                exit = true;
                break;
            case Print: //print all messages
                updateLiveChat();
                printAllMessages();
                break;
            case Refresh: //refresh updates
                //TODO do better
                io.printMessage("Your updates");
                //give time to others threads to print their status updates
                sleep(100);
                break;
            case Secret: //send private message
                postToPrivateChat();
                break;
            case Start:
                stub.startGame(playerName);
                break;
            case Move:
                Move move = createMove();
                stub.postMove(playerName,move);
                break;
            case Peek:
                peekElement();
                break;
            case Message:
                postToChat();
                break;
            default: //post message to chat
                io.printErrorMessage("Invalid Command");
        }
    }

    /**
     * Downloads all the messages that are present on server and missing in local copy
     * @throws RemoteException
     */
    public void updateLiveChat() throws RemoteException {
        chat = stub.updateLiveChat();
    }

    /**
     * Print all messages in local copy of chat. If none is present "No message yet" will be printed
     */
    public void printAllMessages(){
        if(chat == null || chat.size() == 0){
            io.printMessage("No messages yet");
            return;
        }
        List<String> toPrint = chat.getAllMessages().stream().map(mes -> mes.toString()).collect(Collectors.toList());
        toPrint.add(0, io.messageFormat("Here are all messages"));
        io.multiPrint(toPrint);
    }

    public static boolean checkValidReceiver(ChatMessage message, String receiverName){
        if (message.getClass().equals(PrivateChatMessage.class)){
            if(!((PrivateChatMessage) message).getReciever().equals(receiverName))
                return false;
        }
        return true;
    }
    public void postToChat() throws Exception {
        List<String> fields = new ArrayList<>();
        fields.add("message");
        Map<String,String> header = io.multiScan(fields);
        stub.postToLiveChat(
                playerName,
                header.get("message"));
    }
    public void postToPrivateChat() throws Exception {
        List<String> fields = new ArrayList<>();
        fields.add("receiver");
        fields.add("message");
        Map<String,String> header = io.multiScan(fields);
        stub.postSecretToLiveChat(
                playerName,
                header.get("receiver"),
                header.get("message"));
    }

    private Move createMove(){
        //TODO implement
        return null;
    }
    private void peekElement(){
        io.printMessage("What do you want to see (Board/Shelf)");
        String choice = io.scan();
        if(!inputSanitizer.isValidMessage(choice)){
            io.printErrorMessage("Please enter valid command");
            return;
        }
        switch (choice.toLowerCase()){
            case("board"):
                //TODO show board
                break;
            case("shelf"):
                io.printMessage("Select the player:");
                String player = io.scan();
                if(!inputSanitizer.isValidName(choice)){
                    io.printErrorMessage("Please enter valid command");
                    return;
                }
                //TODO show player's shelf
                break;
            default:
                io.printErrorMessage("Not valid element");
        }
    }
    @Override
    public void run(){
        io.printMessage("Make your moves! Tell your unrequested opinions!"); //introduction message after login
        loopCommands();
    }
}
