package it.polimi.ingsw.client.model;

import it.polimi.ingsw.client.controller.ClientController;
import it.polimi.ingsw.client.controller.gui.sceneControlles.ChatController;
import it.polimi.ingsw.client.controller.gui.ClientControllerGUI;
import it.polimi.ingsw.client.controller.gui.SceneEnum;
import it.polimi.ingsw.client.controller.gui.sceneControlles.HomeScreenController;
import it.polimi.ingsw.client.controller.gui.sceneControlles.PlayerShelvesController;
import it.polimi.ingsw.client.controller.gui.sceneControlles.WaitingLobbyController;
import it.polimi.ingsw.shared.Chat;
import it.polimi.ingsw.shared.JSONFilePath;
import it.polimi.ingsw.shared.JsonBadParsingException;
import it.polimi.ingsw.shared.RemoteInterfaces.ClientRemote;
import it.polimi.ingsw.shared.model.*;
import javafx.application.Platform;
import org.json.simple.JSONObject;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * For the general behaviour please refer to the javadoc of ClientController.
 * This object is used to wrap the GUI and call the proper methods of JavaFX.
 * Here a copy of the model is not needed.
 */
public class ClientModelGUI extends UnicastRemoteObject implements ClientModel, ClientRemote {
    private final String playerName;
    private boolean itsMyTurn;
    private Chat chat;
    private Board board;
    private final Map<String, Shelf> playersShelves;
    private PlayerGoal playerGoal;
    private final List<CommonGoal> commonGoalList;
    private final List<String> players;
    private boolean gameStarted;
    private boolean gameEnded;
    private Map<String, Integer> leaderBoard;
    private final Thread pingListener;
    private final Object pingLock;
    private final ClientControllerGUI controller;

    public ClientModelGUI(String playerName, ClientControllerGUI controller) throws RemoteException {
        super();
        this.playerName=playerName;
        this.itsMyTurn=false;
        this.chat = new Chat();
        this.board = null;
        this.playersShelves = new HashMap<>();
        this.playerGoal = null;
        this.commonGoalList = new ArrayList<>();
        this.players = new ArrayList<>();
        this.gameStarted = false;
        this.itsMyTurn = false;
        this.gameEnded = false;
        this.leaderBoard = new HashMap<>();

        this.pingLock = new Object();
        this.pingListener = ClientController.getThread(pingLock);

        this.controller = controller;
    }

    /**
     * This method is used to return the name of
     * the players using this client.
     *
     * @return String, player's name.
     */
    public String getPlayerName() {
        return playerName;
    }

    public Chat getChat() {
        return new Chat(this.chat);
    }

    public Board getBoard() {
        if (board==null) {
            throw new RuntimeException("The client contains an invalid board: null");
        }
        try {
            return new Board(this.board);
        } catch (JsonBadParsingException e) {
            throw new RuntimeException("The client contains an invalid board: " + e.getMessage());
        }
    }

    /**
     * This method is used to observe if the game has started.
     * @return Boolean, true if the game has started.
     */
    @Override
    public boolean gameIsStarted(){
        return gameStarted;
    }

    public Map<String, Shelf> getPlayersShelves() {
        return playersShelves;
    }

    public PlayerGoal getPlayerGoal() {
        return playerGoal;
    }

    public List<CommonGoal> getCommonGoalList() {
        return commonGoalList;
    }

    public List<String> getPlayers() {
        return players;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    @Override
    public boolean gameEnded() throws RemoteException {
        return gameEnded;
    }

    /**
     * @return Map: player's name - points
     */
    public Map<String, Integer> getLeaderBoard() {
        return new HashMap<>(leaderBoard);
    }

    /**
     * This method is used when a player picks a tile
     * from the board. It sends the message
     * to the remote view to remove the tile
     * from the board.
     *
     * @param position position
     */
    public void pickedFromBoard(JSONObject position) {
        final Position pos = new Position(position);
        try {
            board.pickTile(pos);
        } catch (BadPositionException e) {
            throw new RuntimeException("Received invalid position from server: " + e.getMessage());
        }
        final HomeScreenController sceneController =
                (HomeScreenController) controller.getSceneController(SceneEnum.home);
        if(sceneController!=null)
            Platform.runLater(() -> {
                sceneController.removeFromBoard(pos);
            });
    }

    /**
     * This method is used to transfer the whole board
     * to the remote view,
     * it uses a json string.
     *
     * @param board JSONObject
     */
    public void refreshBoard(JSONObject board) {
        try {
            this.board = new Board(board, new ArrayList<>());
        } catch (JsonBadParsingException e) {
            throw new RuntimeException("Received invalid position from server: " + e.getMessage());
        }

        final HomeScreenController sceneController =
                (HomeScreenController) controller.getSceneController(SceneEnum.home);
        if(sceneController!=null)
            Platform.runLater(() -> {
                sceneController.setBoard(this.board);
            });
    }

    /**
     * This method is used when a player inserts a single
     * tile into his shelf.
     * It is used to send the message to the remote view
     * of the client in order to insert the tile
     * into the shelf.
     *
     * @param player String name of the player that moved the tile
     * @param column destination column of the shelf
     * @param tile   Tile to insert
     */
    public void putIntoShelf(String player, int column, Tile tile) {
        try{
            final Shelf shelf = playersShelves.get(player);
            shelf.insertTile(tile, column);
            playersShelves.replace(player, shelf);
        } catch (BadPositionException e) {
            throw new RuntimeException("Received invalid position from server: " + e.getMessage());
        }

        if(player.equalsIgnoreCase(playerName)) {
            final HomeScreenController sceneController =
                    (HomeScreenController) controller.getSceneController(SceneEnum.home);
            if(sceneController!=null)
                Platform.runLater(() -> {
                    sceneController.putIntoShelf(column, tile);
                });
        } else {
            final PlayerShelvesController sceneController =
                    (PlayerShelvesController) controller.getSceneController(SceneEnum.playerShelves);
            if(sceneController!=null)
                Platform.runLater(() -> {
                    sceneController.putIntoShelf(player, column, tile);
                });
        }
    }

    /**
     * This method is used to transfer the whole shelf
     * of a player to the remote view of the client,
     * it uses a json string.
     *
     * @param player name of the player
     * @param jsonShelf  JSONObject
     */
    public void refreshShelf(String player, JSONObject jsonShelf) {
        playersShelves.remove(player);
        final Shelf shelf;
        try {
            shelf = new Shelf(jsonShelf);
            this.playersShelves.put(player, shelf);
        } catch (JsonBadParsingException e) {
            throw new RuntimeException("Received invalid jsonShelf from server: " + e.getMessage());
        }

        if(player.equalsIgnoreCase(playerName)) {
            final HomeScreenController sceneController =
                    (HomeScreenController) controller.getSceneController(SceneEnum.home);
            if(sceneController!=null)
                Platform.runLater(() -> {
                    sceneController.updateShelf(shelf);
                });
        } else {
            final PlayerShelvesController sceneController =
                    (PlayerShelvesController) controller.getSceneController(SceneEnum.playerShelves);
            if(sceneController!=null)
                Platform.runLater(() -> {
                    sceneController.refreshShelf(player, shelf);
                });
        }

    }

    /**
     * This method is used to send a chat message to clients.
     *
     * @param sender  Player's name
     * @param message String message
     */
    public void postChatMessage(String sender, String message) {
        chat.addMessage(sender, message);

        final ChatController sceneController = (ChatController) controller.getSceneController(SceneEnum.chat);
        if (sceneController!=null)
            Platform.runLater(() -> {
                sceneController.postChatMessage(sender, message);
            });

        final WaitingLobbyController waitingLobbyController = (WaitingLobbyController) controller.getSceneController(SceneEnum.lobbyWaiting);
        if (waitingLobbyController!=null)
            Platform.runLater(() -> {
                waitingLobbyController.postChatMessage(sender, message);
            });

        if(controller.getCurrentScene().equals(SceneEnum.home) || controller.getCurrentScene().equals(SceneEnum.playerShelves)) {
            final HomeScreenController homeScreenController = (HomeScreenController) controller.getSceneController(SceneEnum.home);
            if(homeScreenController!=null)
                Platform.runLater(() -> {
                    homeScreenController.setNewMessage(true);
                });
        }
    }

    /**
     * This method is used to send the whole chat to the client,
     * it is used when a refresh is needed.
     *
     * @param chat Chat object
     */
    public void refreshChat(Chat chat) {
        this.chat = chat;

        final ChatController sceneController = ((ChatController) controller.getSceneController(SceneEnum.chat));
        if (sceneController!=null)
            Platform.runLater(() -> {
                sceneController.refreshChat(chat);
            });

        final WaitingLobbyController waitingLobbyController = (WaitingLobbyController) controller.getSceneController(SceneEnum.lobbyWaiting);
        if (waitingLobbyController!=null)
            Platform.runLater(() -> {
                waitingLobbyController.refreshChat(chat);
            });
    }

    /**
     * This function throws a RuntimeException if any element
     * of the model is not set properly.
     */
    private void ensureModelIsSet() {
        if (board==null) throw new RuntimeException("Board null after the game has started");
        if(commonGoalList.isEmpty()) throw new RuntimeException("Common goals list empty after game has started");
        if(playersShelves.isEmpty()) throw new RuntimeException("Player shelves list empty after game has started");
        if(playerGoal==null) throw new RuntimeException("Personal goal null after game has started");
    }


    /**
     * This method is used when the lobby is ready and the
     * admin started the game.
     * @param newMatch true if the game is new,
     *        false if it was loaded from a save or the player
     *        reconnected.
     */
    @Override
    public void gameStarted(boolean newMatch) {
        ensureModelIsSet();
        Platform.runLater(() -> {
            controller.setNewMatch(newMatch);
            controller.loadScene(SceneEnum.home);
        });
        gameStarted = true;

    }

    /**
     * This function is used when the turn of a player ends.
     *
     * @param player Name of the player that will play next.
     */
    @Override
    public void nextTurn(String player) {
        if (player.equals(this.playerName)) {
            itsMyTurn=true;
        } else {
            itsMyTurn=false;
        }

        final HomeScreenController sceneController =
                (HomeScreenController) controller.getSceneController(SceneEnum.home);
        if(sceneController!=null)
            Platform.runLater(() -> {
                sceneController.setMyTurn(itsMyTurn);
            });
    }

    /**
     * @return True if the player need to play in the current turn
     */
    @Override
    public boolean isItMyTurn() {
        return itsMyTurn;
    }

    /**
     * This method is used when a player achieves
     * a common goal and pops points from
     * its stack.
     * It is also used to init the common goal.
     *
     * @param id     ID of the common goal
     * @param points Copy of the stack with points that
     *               can still be achieved
     */
    @Override
    public void refreshCommonGoal(int id, List<Integer> points) {
        final CommonGoal commonGoal = new CommonGoal(CommonGoalStrategy.findById(id), points);
        commonGoalList.remove(commonGoal);
        commonGoalList.add(commonGoal);
        commonGoalList.sort((x,y) -> x.getID() > y.getID() ? 1 : -1);
    }

    /**
     * This method is used at the beginning of the game to let
     * the client know its personal goal
     *
     * @param id Int ID of the goal
     */
    @Override
    public void setPlayerGoal(int id) {
        try {
            this.playerGoal = new PlayerGoal(JSONFilePath.PlayerGoals, id);
        } catch (JsonBadParsingException e) {
            throw new RuntimeException("JsonBadParsing exception while loading player goal: " + e.getMessage());
        }

    }

    /**
     * This method is used at the end of the game to
     * send the leaderboard to the client.
     * @param leaderBoard Map: player's name - points
     */
    public void endGame(Map<String, Integer> leaderBoard){
        this.gameEnded = true;
        this.leaderBoard = leaderBoard;
        Platform.runLater(() -> {
            controller.loadScene(SceneEnum.winnerScreen);
        });
    }


    /**
     * This function is used to ensure the client is still connected.
     * Expected return value is "pong".
     */
    @Override
    public String ping() {
        if(pingListener.getState().equals(Thread.State.NEW)) {
            pingListener.start();
        }
        synchronized (pingLock) {
            pingLock.notifyAll();
        }
        return "pong";
    }
}
