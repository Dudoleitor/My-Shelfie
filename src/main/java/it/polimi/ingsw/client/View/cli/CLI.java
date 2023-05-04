package it.polimi.ingsw.client.View.cli;

import it.polimi.ingsw.client.Client_Settings;
import it.polimi.ingsw.client.View.LobbyCommand;
import it.polimi.ingsw.client.View.View;
import it.polimi.ingsw.client.View.LobbySelectionCommand;
import it.polimi.ingsw.shared.model.*;
import it.polimi.ingsw.shared.*;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static it.polimi.ingsw.client.Client_Settings.*;

public class CLI extends View {
    private static Lock cli_lock;
    private InputSanitizer inputSanitizer;
    private Scanner scanner;
    private String placeHolder = stdPlaceHolder;
    private static boolean placeHolderToRestore = false;

    /**
     * Constructor. The cli_lock is built as a Singleton
     */
    public CLI(){
        if(cli_lock == null){
            cli_lock = new ReentrantLock();
        }
        inputSanitizer = new InputSanitizer();
        scanner = new Scanner(System.in);
    }

    @Override
    public LobbyCommand askCommand(){
            String input;
            input = scan();
            //Invalid command
            if(!inputSanitizer.isValidMessage(input)){
                synchronized (cli_lock){
                    printErrorMessage("Invalid format");
                }
                return LobbyCommand.Invalid;
            }
            else{
                return LobbyCommand.stringToCommand(input);
            }
    }

    @Override
    public void notifyExit() {
        synchronized (cli_lock){
            printErrorMessage("You quit!");
        }
    }

    /**
     * Print all messages in local copy of chat. If none is present "No message yet" will be printed
     */
    @Override
    public void showAllMessages(Chat chat){
        synchronized (cli_lock){
            skipPlaceHolder();
            if(chat == null || chat.size() == 0){
                printMessage("No messages yet");
                return;
            }
            chat.getAllMessages().stream().map(mes -> mes.toString()).forEach(System.out::println);
            restorePlaceHolder();
        }
    }
    @Override
    public Map<String,String> getMessageFromUser(){
        synchronized (cli_lock){
            List<String> fields = new ArrayList<>();
            fields.add("message");
            return fieldsScan(fields);
        }
    }

    @Override
    public Map<String,String> getPrivateMessageFromUser(){
        synchronized (cli_lock){
            List<String> fields = new ArrayList<>();
            fields.add("receiver");
            fields.add("message");
            return fieldsScan(fields);
        }
    }

    @Override
    public Move getMoveFromUser(Board board, Shelf shelf){
        PartialMove pm = new PartialMove();
        int column = 0;
        Move move = null;
        synchronized (cli_lock){
            printMessage("Write your move");
            int posNum = getNumberOfTilesFromUser(shelf);
            pm = getPartialMoveFromUser(board,posNum);
            column = getColumnFromUser(shelf,posNum);
            move = new Move(pm,column);
            printMessage("Your move:\n" + move.toString());
        }
        return move;
    }

    /**
     * Get from input a valid number of tiles to pick according to game status
     * @param shelf the player's sheld
     * @return the first valid number chosen by user
     */
    private int getNumberOfTilesFromUser(Shelf shelf){
        String posNumStr = "";
        int posNum = 0;
        int maxShelfCapacity = shelf.getHighestColumn();
        boolean validInput = false;

        while(!validInput){
            validInput = true;
            printMessage("How many tiles do you want to pick?:");
            posNumStr =  scan();

            //assure it's a number
            if(!inputSanitizer.isInteger(posNumStr)){
                printErrorMessage("Please insert an integer");
                validInput = false;
                continue;
            }

            posNum = Integer.parseInt(posNumStr);

            //assure it's a number that is valid and can fit in at least one column
            if(!(posNum > 0 && posNum <= 3 && posNum <= maxShelfCapacity)){
                printErrorMessage("Invalid number");
                validInput = false;
            }
        }
        return posNum;
    }

    /**
     * Build the partila move from the user input
     * @param board the board
     * @param posNum the chosen number of positions. The choice of tiles can reduce it
     * @return the partial move
     */
    private PartialMove getPartialMoveFromUser(Board board, int posNum){
        PartialMove pm = new PartialMove();
        boolean validInput = false;
        while(!validInput){
            validInput = true;
            for(int i = 0; i < posNum; i++){
                try{

                    //assure that there are valid positions
                    if(board.getValidPositions(pm).isEmpty()){
                        printErrorMessage("There are no more tiles to pick");
                        posNum = i;
                        break;
                    }

                    //show valid moves
                    printMessage("Here is the list of all the valid moves:");
                    for(Position p : board.getValidPositions(pm)){
                        System.out.println("> "+p.toString());
                    }

                    //get position
                    printMessage("Position #"+String.valueOf(i+1)+":");
                    String stringPos = scan();
                    Position pos = Position.fromString(stringPos);

                    //if invalid
                    if(pos == null || !board.getValidPositions(pm).contains(pos)){
                        printErrorMessage("Please enter a valid position");
                        i--; //ask again the same position
                    }
                    else {
                        //add to move
                        pm.addPosition(pos);
                    }
                }
                catch (Exception e){
                    printErrorMessage("Invalid format");
                    validInput = false;
                }
            }
        }
        return pm;
    }

    /**
     * Get from input a valid column from user
     * @param shelf the player's sheld
     * @param numPos the number of tiles to put in shelf
     * @return the first valid number column by user
     */
    private int getColumnFromUser(Shelf shelf, int numPos){
        String scannedColumn = "";
        boolean validInput = false;
        int column = 0;
        while(!validInput){

            validInput = true;
            printMessage("Choose the column");
            printMessage("Here is a list of the valid columns");
            for(int choice : columnsToChose(shelf,numPos)){
                System.out.println("> "+choice);
            }
            scannedColumn = scan(); //scan the input

            //assure it's an integer
            if(!inputSanitizer.isInteger(scannedColumn)){
                validInput = false;
                printErrorMessage("Please enter a number");
                continue;
            }

            column = Integer.valueOf(scannedColumn);

            //assure the number is inside the bounds og shelf
            if(!(column >= 0 && column < shelf.getColumns())){
                validInput = false;
                printErrorMessage("Please enter a valid number");
                continue;
            }

            //assure the tiles will fit int the column
            if(columnHeigth(shelf,column) + numPos > shelf.getRows()){
                printErrorMessage("You cannot insert "+numPos+ " tiles in column "+ column);
                validInput = false;
            }
        }
        return column;
    }

    /**
     * @param shelf
     * @param column
     * @return the height of a column
     */
    private int columnHeigth(Shelf shelf, int column){
        return (int) shelf.allTilesInColumn(column).stream()
                .filter(x -> !x.equals(Tile.Empty)).count();
    }

    /**
     * @param shelf
     * @param posNum
     * @return a list of valid columns to choose from
     */
    private List<Integer> columnsToChose(Shelf shelf, int posNum){
        return  IntStream.range(0,shelf.getColumns()).
                filter(col -> columnHeigth(shelf,col) + posNum <= shelf.getRows()).
                boxed().
                collect(Collectors.toList());
    }

    public void showGameStatus(Board b,List<CommonGoal> commongoals ,Map<String, Shelf> playerShelves, PlayerGoal goal){
        synchronized (cli_lock){
            skipPlaceHolder();
            showBoard(b);
            showCommonGoals(commongoals);
            showShelves(playerShelves);
            showPersonalGoal(goal);
        }
    }

    public void showBoard(Board b) {
        printMessage(b.toString());
    }

    public void showCommonGoals(List<CommonGoal> commonGoalList) {
        String str = "Common Goals:";
        if(commonGoalList.size() == 0){
            str = str.concat(Color.coloredString("None",Color.Yellow));
        }
        for(CommonGoal cg : commonGoalList){
            str = str.concat("\n"+cg.toString());
        }
        printMessage(str);
    }

    public void showShelves(Map<String, Shelf> playerShelves) {
        String output = "Shelves:\n";
        final String spaceBetween = "     ";
        int rows = shelfStringRows(playerShelves);
        for (int k=0; k<shelfStringRows(playerShelves); k++) {  // Iterating over the rows
            for(String player : playerShelves.keySet()) {
                output = output.concat(
                        playerShelves
                                .get(player)
                                .toString()
                                .split("\n")[k]
                );
                output = output.concat(spaceBetween);
            }
            output = output.concat("\n");
        }
        final int shelfRowLenght = shelfStringCols(playerShelves);
        for (String player : playerShelves.keySet()) {
            output = player.length()<=shelfRowLenght ? output.concat(player) : output.concat(player).substring(0, shelfRowLenght);
            int padding = shelfRowLenght - player.length();
            for (int k=0; k<padding; k++) output = output.concat(" ");
            //output = output.concat(spaceBetween);  // MISTERY
        }
        printMessage(output);
    }

    private static int shelfStringRows(Map<String, Shelf> playerShelves) {
        Shelf shelf = playerShelves.get(playerShelves.keySet().stream().findFirst().get());

        return (shelf.toString().split("\n")).length;
    }
    private static int shelfStringCols(Map<String, Shelf> playerShelves) {
        Shelf shelf = playerShelves.get(playerShelves.keySet().stream().findFirst().get());

        return (shelf.toString().split("\n")[0].length());
    }

    public void showPersonalGoal(PlayerGoal goal) {
        printMessage(goal.toString());
    }

    public void showChatMessage(ChatMessage message) {
        synchronized (cli_lock){
            skipPlaceHolder();
            System.out.println(message);
            restorePlaceHolder();
        }
    }

    @Override
    public void showHelp() {
        synchronized (cli_lock){
            String help = "Here are all the commands:\n";
            List<String> commandList = Arrays.stream(LobbyCommand.values()).
                    filter(c -> c != LobbyCommand.Help && c != LobbyCommand.Invalid).
                    map(c -> "    -> " + c.getCode().toUpperCase() + " ["+c.getShortcut()+"] :"+c.getDescription()+"\n").
                    collect(Collectors.toList());
            for(String command : commandList){
                help = help.concat(command);
            }
            printMessage(help);
        }
    }

    @Override
    public void notifyInvalidCommand(){
        printErrorMessage("Please input a valid command");
    }
    @Override
    public String askUserName(){
        synchronized (cli_lock){
            System.out.println(gameLogo);
            String name = "";
            while(!inputSanitizer.isValidName(name)){
                printMessage("Enter your username");
                name = scan();
                if(!inputSanitizer.isValidName(name)){
                    printErrorMessage("Please enter a valid name");
                }
            }
            printMessage("Hello "+name+"!");
            return name;
        }
    }
    @Override
    public void showLobbies(Map<Integer,Integer> availableLobbies, String description){
        synchronized (cli_lock){
            String lobbyMessage = description;
            if (!(availableLobbies == null) && !availableLobbies.isEmpty()) {
                List<String> lobbyList = (List<String>)
                        availableLobbies.keySet().stream().
                                map(x -> "\n    -> Lobby " + x + ":  " + availableLobbies.get(x) + " players in").
                                collect(Collectors.toList());
                for(String mes : lobbyList){
                    lobbyMessage = lobbyMessage.concat(mes);
                }
            } else {
                lobbyMessage = lobbyMessage + "\n       None";
            }
            printMessage(lobbyMessage);
        }
    }
    @Override
    public LobbySelectionCommand askLobby(){
        int lobbyID;
        String id;
        LobbySelectionCommand command;
        synchronized (cli_lock){
            printMessage("Choose a Lobby (ENTER for random, NEW to create a new one, REFRESH to update lobby list):");
            id = scan();
        }
        if(id.isEmpty()){
            return LobbySelectionCommand.Random;
        }
        else if (id.toLowerCase().equals("new")) {
            return LobbySelectionCommand.Create;
        }
        else if (id.toLowerCase().equals("refresh")) {
            return LobbySelectionCommand.Refresh;
        }
        else if(inputSanitizer.isInteger(id)){
            lobbyID = Integer.parseInt(id);
            command = LobbySelectionCommand.Number;
            command.setId(lobbyID);
        }
        else{
            command = LobbySelectionCommand.Invalid;
        }
        return command;
    }
    @Override
    public boolean playAgain(){
        String answer;
        synchronized (cli_lock){
            printMessage("Do you want to play again?");
            answer = scan();
            if(answer.toLowerCase().equals("yes") || answer.toLowerCase().equals("y")){
                return true;
            }
            else {
                return false;
            }
        }
    }

    @Override
    public void setLobbyAdmin(boolean isAdmin){
        if(isAdmin){
            synchronized (cli_lock){
                printMessage("You are the lobby admin!");
            }
            setPlaceHolder(Client_Settings.adminPlaceHolder);
        }
        else{
            setPlaceHolder(Client_Settings.stdPlaceHolder);
        }
    }


    /**
     * Prints the string (decorated as an error message) in a synchronous way
     * => never to be called internally!!
     * @param  message the error message string
     */
    @Override
    public void errorMessage(String message) {
        synchronized (cli_lock){
            skipPlaceHolder();
            printErrorMessage(message);
            restorePlaceHolder();
        }
    }

    /**
     * Prints the string (decorated as a message) in a synchronous way
     * => never to be called internally!!
     * @param message the message string
     */
    @Override
    public void message(String message) {
        synchronized (cli_lock){
            skipPlaceHolder();
            printMessage(message);
            restorePlaceHolder();
        }
    }

    //String decorators
    private void printErrorMessage(String message){
        System.out.println(errorFormat(message));
    }

    private void printMessage(String message){
        System.out.println(messageFormat(message));
    }

    private void skipPlaceHolder(){
        if(placeHolderToRestore){
            System.out.println("");
        }
    }

    private void restorePlaceHolder(){
        if(placeHolderToRestore){
            printPlaceHolder();
        }
    }

    /**
     * A decorator that formats a message
     * @param s the message
     * @return the decorated message (>GAME: mes with colors)
     */
    public String messageFormat(String s){
        return ">"+ Color.coloredString("GAME",GAMEColor)+": " + Color.coloredString(s,messageColor);
    }

    /**
     * Prints the placeholder
     */
    public void printPlaceHolder(){
        placeHolderToRestore = true;
        System.out.print(placeHolder);
    }

    public void setPlaceHolder(String placeHolder){
        this.placeHolder = placeHolder;
    }

    /**
     * A decorator that formats an error message
     * @param s the message
     * @return the decorated message (>GAME: mes with colors)
     */
    public String errorFormat(String s){
        return ">"+ Color.coloredString("GAME",GAMEColor)+": " + Color.coloredString(s,errorColor);
    }

    //Scan methods

    /**
     * Scans the input without locking IO
     * @return the user input
     */
    public String scan(){
        printPlaceHolder();
        String command = scanner.nextLine();
        placeHolderToRestore = false;
        return command;
    }

    /**
     * Lock the IO and scans for all the fields
     * @param fields
     * @return a map that associates each field with the scanned String
     */
    public Map<String,String> fieldsScan(List<String> fields){
        Map<String,String> result = new HashMap();
        String value;
        for(String field : fields){
            printMessage(field+":");
            printPlaceHolder();
            value =  scanner.nextLine();
            placeHolderToRestore = false;
            result.put(field,value);
        }
        return result;
    }
}