package it.polimi.ingsw.shared;
import org.json.simple.JSONObject;

import javax.management.remote.JMXServerErrorException;
import java.rmi.RemoteException;

public class MessageTcp {
    private MessageCommand messageCommand;
    private JSONObject content;

    public MessageTcp(){
        super();
    }

    /**
     * Creates an object that parse a String corresponding to a JsonObject and expose get methods to see the command and the content of message
     * @param message is the message to parse
     */
    public MessageTcp(String message){
        JSONObject jsonMessage = Jsonable.parseString(message);
        MessageCommand command = MessageCommand.valueOfLabel(jsonMessage.get("command").toString());
        JSONObject content = (JSONObject) jsonMessage.get("content");
        this.messageCommand = command;
        if(content != null) {
            this.content = Jsonable.parseString(content.toString());
        }
    }
    public enum MessageCommand { //this is a public enumeration of all possible commands sent over TCP
        Login("login",TypeMessage.ReplyWait),
        GetJoinedLobbies("getJoined", TypeMessage.ReplyWait),
        JoinRandomLobby("joinRandom", TypeMessage.ReplyWait),
        CreateLobby("createLobby", TypeMessage.ReplyWait),
        GetAvailableLobbies("availableLobbies", TypeMessage.ReplyWait),
        JoinSelectedLobby("joinSelected", TypeMessage.ReplyWait),
        PostToLiveChat("postChat", TypeMessage.ReplyWait),
        PostSecretToLiveChat("postSecret", TypeMessage.ReplyWait),
        Quit("quit", TypeMessage.ReplyWait),
        MatchHasStarted("matchStarted", TypeMessage.ReplyWait),
        PostMove("move", TypeMessage.ReplyWait),
        StartGame("start", TypeMessage.ReplyWait),
        IsLobbyAdmin("isAdmin", TypeMessage.ReplyWait),
        PickedFromBoard("pickedTile", TypeMessage.Update),
        RefreshBoard("refreshBoard", TypeMessage.Update),
        PutIntoShelf("putShelf",TypeMessage.Update),
        RefreshShelf("refreshShelf", TypeMessage.Update),
        ChatMessageUpdate("chatUpdate", TypeMessage.Update),
        RefreshChat("refreshChat", TypeMessage.Update),
        NotifyStart("notifyStart", TypeMessage.Update),
        UpdateTurn("updateTurn", TypeMessage.Update),
        RefreshCommonGoals("refreshGoals", TypeMessage.Update),
        SetPlayerGoal("setGoal", TypeMessage.Update),
        Disconnect("disconnect", TypeMessage.Update),
        Ping("ping", TypeMessage.Update),




        ;
        public enum TypeMessage{
            ReplyWait,
            Update
        }

        private final String label;
        private final TypeMessage type;
        MessageCommand(String tag, TypeMessage type){
            this.label = tag;
            this.type = type;
        }
        public static MessageCommand valueOfLabel(String label) {
            for (MessageCommand e : values()) {
                if (e.label.equals(label)) {
                    return e;
                }
            }
            throw new RuntimeException("command not found");
        }
        public TypeMessage typeOfCommand(){
            return type;
        }

        @Override
        public String toString() {return label;}
    }


    public void setCommand(MessageCommand messageCommand){
        this.messageCommand = messageCommand;
    }
    public void setContent(JSONObject content) {
        this.content = content;
    }
    public boolean isReplyMessage(){
        boolean result;
        if(messageCommand == null){
            result = false;
        } else {
            result = messageCommand.typeOfCommand().equals(MessageCommand.TypeMessage.ReplyWait);

        }
        return result;
    }
    public boolean isUpdateMessage(){
        boolean result;
        if(messageCommand == null){
            result = false;
        } else {
            result = messageCommand.typeOfCommand().equals(MessageCommand.TypeMessage.Update);

        }
        return result;

    }

    @Override
    public String toString() {
        JSONObject message = new JSONObject();
        message.put("command", this.messageCommand.toString());
        message.put("content", this.content);
        return message.toJSONString();
    }

    public MessageCommand getCommand(){
        return messageCommand;
    }

    public JSONObject getContent(){
        if (content != null)
            return content;
        else
            return null;

    }
}
