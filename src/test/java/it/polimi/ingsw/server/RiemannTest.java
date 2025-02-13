package it.polimi.ingsw.server;

import it.polimi.ingsw.client.controller.cli.CLI_IO;
import it.polimi.ingsw.server.clientonserver.Client;
import it.polimi.ingsw.shared.*;
import it.polimi.ingsw.shared.model.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
public class RiemannTest { //an integration test
    static final boolean verbose = false;

    @Test
    void wholeMatchTest() throws JsonBadParsingException {
        List<String> playerNames = new ArrayList<>();
        playerNames.add("fridgeieri");
        playerNames.add("fridgeoggi");
        playerNames.add("fridgedomani");
        playerNames.add("friededopodomani");
        List<Client> clients = playerNames.stream().map(ClientStub::new).collect(Collectors.toList());
        Controller c = new Controller(clients);
        c.setTurn_testing_only("fridgeieri");
        List<Player> playerList = null;
        try{
            playerList = (List<Player>) playerNames.stream().map(p -> {
                try {
                    return new Player(p,new Shelf(GameSettings.shelfRows, GameSettings.shelfColumns), new PlayerGoal(JSONFilePath.PlayerGoals));
                } catch (JsonBadParsingException e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList());
        }
        catch (Exception e){
            fail();
        }

        //TURN 0
        //contains the correct players
        assertTrue(c.getPlayers().containsAll(playerList) && c.getPlayers().size()==playerList.size());

        //valid commongoals (hardcoded valid values 1-12)
        assertTrue(c.getCommonGoals().stream().allMatch(cg -> cg.getID() >= 1 && cg.getID() <= 12));

        //start turn from fridgeieri
        assertEquals("fridgeieri",c.getCurrentPlayerName());

        //the Board is a correct empty board for 4 players -- ASSERTION REMOVED! Controller must fill the board
        //assertTrue(c.getBoard().sameBoard(new Board(4)));
        //the first player is actually the first
        assertEquals(playerNames.get(0),c.getCurrentPlayerName());

        //all shelves are empty
        Shelf emptyShelf = new Shelf(GameSettings.shelfRows, GameSettings.shelfColumns);
        for(Player p : c.getPlayers()) {
            assertTrue(c.getShelves().get(p.getName()).equals(emptyShelf));
        }
        printAll(c);

        //TURN 1
        c.prepareForNextPlayer();
        printAll(c);
        assertFalse(c.getBoard().sameBoard(new Board(4)));
        PartialMove pm = new PartialMove();
        List<Tile> picked;
        try {
            picked = new ArrayList<>();
            picked.add(c.getBoard().getTile(0,3));
            pm.addPosition(new Position(0,3));
            picked.add(c.getBoard().getTile(0,4));
            pm.addPosition(new Position(0,4));
        } catch (InvalidMoveException e) {
            throw new RuntimeException(e);
        } catch (BadPositionException e) {
            throw new RuntimeException(e);
        }
        Move m = new Move(pm,0);
        c.moveTiles(playerList.get(0).getName(),m);
        printAll(c);
        try {
            //The removed tiles are empty
            assertEquals(Tile.Empty,c.getBoard().getTile(0,3));
            assertEquals(Tile.Empty,c.getBoard().getTile(0,4));

            //Next turn has been called
            assertEquals(playerNames.get(1),c.getCurrentPlayerName());
            assertTrue(c.getShelves().get(playerNames.get(0)).allTilesInColumn(0).containsAll(picked));
            assertEquals(picked.get(0),c.getShelves().get(playerNames.get(0)).getTile(5,0));
            assertEquals(picked.get(1),c.getShelves().get(playerNames.get(0)).getTile(4,0));
        } catch (BadPositionException e) {
            throw new RuntimeException(e);
        }

        //TURN 2
        c.prepareForNextPlayer();
        printAll(c);
        assertFalse(c.getBoard().sameBoard(new Board(4)));
        pm = new PartialMove();
        picked.clear();
        try {
            picked.add(c.getBoard().getTile(1,3));
            pm.addPosition(new Position(1,3));
            picked.add(c.getBoard().getTile(1,4));
            pm.addPosition(new Position(1,4));
            picked.add(c.getBoard().getTile(1,5));
            pm.addPosition(new Position(1,5));
        } catch (InvalidMoveException e) {
            throw new RuntimeException(e);
        } catch (BadPositionException e) {
            throw new RuntimeException(e);
        }
        m = new Move(pm,0);
        c.moveTiles(playerList.get(1).getName(),m);
        printAll(c);
        try {
            //The removed tiles are empty
            assertEquals(Tile.Empty,c.getBoard().getTile(1,3));
            assertEquals(Tile.Empty,c.getBoard().getTile(1,4));
            assertEquals(Tile.Empty,c.getBoard().getTile(1,5));

            //Next turn has been called
            assertEquals(playerNames.get(2),c.getCurrentPlayerName());
            assertTrue(c.getShelves().get(playerNames.get(1)).allTilesInColumn(0).containsAll(picked));
            assertEquals(picked.get(0),c.getShelves().get(playerNames.get(1)).getTile(5,0));
            assertEquals(picked.get(1),c.getShelves().get(playerNames.get(1)).getTile(4,0));
            assertEquals(picked.get(2),c.getShelves().get(playerNames.get(1)).getTile(3,0));
        } catch (BadPositionException e) {
            throw new RuntimeException(e);
        }

        //TURN 3 (malicious player turn)
        c.prepareForNextPlayer();
        printAll(c);
        assertFalse(c.getBoard().sameBoard(new Board(4)));
        pm = new PartialMove();
        picked.clear();
        try {
            picked.add(c.getBoard().getTile(1,3));
            pm.addPosition(new Position(1,3));
            picked.add(c.getBoard().getTile(1,4));
            pm.addPosition(new Position(1,4));
            picked.add(c.getBoard().getTile(1,5));
            pm.addPosition(new Position(1,5));
        } catch (InvalidMoveException e) {
            throw new RuntimeException(e);
        } catch (BadPositionException e) {
            throw new RuntimeException(e);
        }
        m = new Move(pm,0);
        try{
            //Wrong player
            c.moveTiles(playerList.get(1).getName(),m);
        }
        catch (Exception e){
            assertEquals(ControllerGenericException.class, e.getClass());
            assertEquals("Player is not the current player",e.getMessage());
        }
        //no nextTurn
        assertEquals(playerNames.get(2),c.getCurrentPlayerName());
        try{
            //Move with empty Tiles
            c.moveTiles(playerList.get(2).getName(),m);
        }
        catch (Exception e){
            assertEquals(ControllerGenericException.class, e.getClass());
            assertEquals("Error invalid move",e.getMessage());
        }
        //no nextTurn
        assertEquals(playerNames.get(2),c.getCurrentPlayerName());

        pm = new PartialMove();
        picked.clear();
        try {
            picked.add(c.getBoard().getTile(2,3));
            pm.addPosition(new Position(2,3));
            picked.add(c.getBoard().getTile(2,4));
            pm.addPosition(new Position(2,4));
            picked.add(c.getBoard().getTile(2,6));
            pm.addPosition(new Position(2,6));
        } catch (InvalidMoveException e) {
            throw new RuntimeException(e);
        } catch (BadPositionException e) {
            throw new RuntimeException(e);
        }
        m = new Move(pm,0);
        try{
            //Move with non aligned tiles
            c.moveTiles(playerList.get(2).getName(),m);
        }
        catch (Exception e){
            assertEquals(ControllerGenericException.class, e.getClass());
            assertEquals("Error invalid move",e.getMessage());
        }
        //no nextTurn
        assertEquals(playerNames.get(2),c.getCurrentPlayerName());

        pm = new PartialMove();
        picked.clear();
        m = new Move(pm,0);
        try{
            //Move with no tiles
            c.moveTiles(playerList.get(2).getName(),m);
        }
        catch (Exception e){
            assertEquals(ControllerGenericException.class, e.getClass());
            assertEquals("Error invalid move",e.getMessage());
        }
        //no nextTurn
        printAll(c);
        assertEquals(playerNames.get(2),c.getCurrentPlayerName());

        try{
            //Null move
            c.moveTiles(playerList.get(2).getName(),null);
        }
        catch (Exception e){
            assertEquals(ControllerGenericException.class, e.getClass());
            assertEquals("Invalid move",e.getMessage());
        }
        //no nextTurn
        assertEquals(playerNames.get(2),c.getCurrentPlayerName());
    }

    private void printAll(Controller c) throws JsonBadParsingException {
        if(verbose){
            CLI_IO cliIO = new CLI_IO();
            cliIO.showBoard(c.getBoard());
            cliIO.showShelves(c.getShelves());
        }
    }
}
