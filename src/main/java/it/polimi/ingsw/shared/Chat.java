package it.polimi.ingsw.shared;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class Chat implements Serializable {
    private List<ChatMessage> chatMessages;
    private Map<String, Color> MapColorPlayer;
    public Chat(){
        chatMessages =  Collections.synchronizedList(new ArrayList<>());
        MapColorPlayer = new HashMap<>();
    }
    public Chat(Chat toClone){
        chatMessages =  Collections.synchronizedList(new ArrayList<>());
        chatMessages.addAll(toClone.getAllMessages());
        MapColorPlayer = new HashMap<>();
    }
    public Chat(JSONObject jsonChat){
        chatMessages = new ArrayList<>();
        JSONArray jsonArray = (JSONArray) jsonChat.get("messageList");
        MapColorPlayer = new HashMap<>();
        for(Object c : jsonArray){
            if(!((JSONObject) c).containsKey("receiver"))
                chatMessages.add(new ChatMessage((JSONObject) c));
            else
                chatMessages.add(new PrivateChatMessage((JSONObject)c));

        }
    }
    public void add(ChatMessage newMessage){
        chatMessages.add(newMessage);
    }
    public void addMessage(String sender,String text){
        if(!MapColorPlayer.containsKey(sender)){
            addPlayer(sender);
        }
        ChatMessage message = new ChatMessage(
                sender,
                text,
                MapColorPlayer.get(sender));
        chatMessages.add(message);
    }

    public void addMessage(ChatMessage message) {
        this.addMessage(message.getSender(), message.getMessage());
    }

    public void add(PrivateChatMessage newSecret){
        chatMessages.add(newSecret);
    }
    public void addSecret(String sender,String receiver,String text){
        if(!MapColorPlayer.containsKey(sender)){
            addPlayer(sender);
        }
        PrivateChatMessage message = new PrivateChatMessage(
                sender,
                receiver,
                text,
                MapColorPlayer.get(sender));
        chatMessages.add(message);
    }
    private void addPlayer(String sender){
        Color playerColor = Color.Purple;
        while(getAllColors().contains(playerColor)){
            playerColor = Color.getRandomColor();
        }
        MapColorPlayer.put(sender, playerColor);
    }

    private List<Color> getAllColors(){
        return  MapColorPlayer.values().
                stream().
                distinct().
                collect(Collectors.toList());
    }
    public int size(){
        return chatMessages.size();
    }
    public ChatMessage get(int index){
        return chatMessages.get(index);
    }
    public ChatMessage getLast(){
        if(chatMessages == null || chatMessages.isEmpty()){
            return null;
        }
        else {
            return chatMessages.get(chatMessages.size()-1);
        }
    }
    public List<ChatMessage> getAllMessages(){
        List<ChatMessage> allmessages = new ArrayList<>(chatMessages);
        return allmessages;
    }

    public JSONObject toJson(){
        JSONObject jsonChat = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for(ChatMessage c : chatMessages){
            jsonArray.add(c.toJson());
        }
        jsonChat.put("messageList", jsonArray);
        return jsonChat;
    }
}
