package it.polimi.ingsw.RMI;

import it.polimi.ingsw.shared.Color;

import java.io.File;

public class CLIPrinter {
    //Default Colors
    public static Color messageColor = Color.Yellow;
    public static Color GAMEColor = Color.Purple;
    public static Color errorColor = Color.Red;

    //Methods to format CLI messages

    /**
     * A decorator that formats a message
     * @param s the message
     * @return the decorated message (>GAME: mes with colors)
     */
    public String messageFormat(String s){
        return ">"+ Color.coloredString("GAME",GAMEColor)+": " + Color.coloredString(s,messageColor);
    }

    public void printPlaceHolder(){
        System.out.print("$:");
    }

    /**
     * A decorator that formats an error message
     * @param s the message
     * @return the decorated message (>GAME: mes with colors)
     */
    public String errorFormat(String s){
        return ">"+ Color.coloredString("GAME",GAMEColor)+": " + Color.coloredString(s,errorColor);
    }
    public void printMessage(String s){
        System.out.println(messageFormat(s));
    }
    public void printErrorMessage(String s){
        System.out.println(errorFormat(s));
    }

}

