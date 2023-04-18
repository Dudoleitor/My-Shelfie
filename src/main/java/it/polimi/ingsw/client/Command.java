package it.polimi.ingsw.client;

import java.util.Arrays;

public enum Command {
        Exit("exit","Quit the game"),
        Print("print","Print all messages in chat"),
        Secret("secret","Send a private message to a user"),
        Message("message","Send a message to all members of lobby"),
        Start("start","Start the game"),
        Move("move","Make your move"),
        Peek("peek","See an element of the board"),
        Help("help",""),
        Invalid("Invalid","");
        private String code;
        private String description;
        Command(String code,String description){
            this.code = code;
            this.description = description;
        }
        public static Command stringToCommand(String input){
            //return the command that matches the user input.
            //If no match return invalid
            return  Arrays.stream(Command.values()).
                    filter(com -> com.getCode()
                            .equals(input.toLowerCase())).
                    findFirst().
                    orElse(Command.Invalid);
        }
        public String getCode(){
                return code;
        }
        public String getDescription(){return description;}
}
