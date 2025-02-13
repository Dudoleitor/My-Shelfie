package it.polimi.ingsw.shared.model;

import it.polimi.ingsw.shared.Color;
import it.polimi.ingsw.shared.JsonBadParsingException;
import it.polimi.ingsw.shared.Jsonable;
import it.polimi.ingsw.shared.virtualview.VirtualShelf;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.LongStream;


public class Shelf implements Jsonable, Grid {
    private final Tile[][] tiles; //matrix coordinate (0,0) is the top-left corner of the shelf
    private final int rows;
    private final int columns;

    private VirtualShelf virtualShelf;

    //Constructors
    /**
     * generate Shelf by input parameters
     * @param rows is num of rows
     * @param columns is num of columns
     */
    public Shelf(int rows, int columns) { //initialize a shelf with Empty tiles
        if(rows <= 0 || columns <= 0){
            throw new ShelfRuntimeException("Error while generating shelf : dimensions are no valid numbers");
        }
        this.rows = rows;
        this.columns = columns;
        tiles = new Tile[rows][columns];
        for (int i = 0; i<rows; i++){ //initialize tiles as a Tile.Empty matrix
            for (int j = 0; j<columns; j++){
                tiles[i][j] = Tile.Empty;
            }
        }
    }

    /**
     * generate Shelf by copy of another Shelf
     * @param s is Shelf to copy from
     */
    public Shelf(Shelf s) {
        try{
            if(s.isNotValid()){
                throw new ShelfRuntimeException("Error while creating Shelf : input Shelf has not a valid configuration");
            }
            rows = s.getRows();
            columns = s.getColumns();
            tiles = new Tile[rows][columns];
            for(int i = 0; i<rows; i++){ //copy all Tiles from Shelf s to tiles
                for(int j = 0; j<columns; j++){
                    tiles[i][j] = s.getTile(i,j);
                }
            }
        } catch(NullPointerException e) {
            throw new ShelfRuntimeException("Error while creating Shelf : input Shelf is null pointer");
        } catch (BadPositionException e) {
            throw new ShelfRuntimeException("Error while creating Shelf : input shelf size is illegal");
        }
    }

    /**
     * Loads a Shelf from Json Object
     * @param jsonShelf is the JSONObject containing the parameters necessary to build the Shelf object
     * @throws JsonBadParsingException when a problem in the creation of shelf occurs
     */
    public Shelf(JSONObject jsonShelf) throws JsonBadParsingException {
        rows = Math.toIntExact((long) (jsonShelf.get("numRows")));
        columns = Math.toIntExact((long) (jsonShelf.get("numColumns")));
        tiles = new Tile[rows][columns];

        JSONArray array_shelf = (JSONArray) jsonShelf.get("matrix"); //matrix is the copy of the shelf
        JSONArray shelfLine;

        Tile t;
        try {
            for (int i = 0; i < rows; i++) { //copy entire matrix to shelf
                shelfLine = (JSONArray) array_shelf.get(i); //acquire line of matrix
                for (int j = 0; j < columns; j++) {
                    t = Tile.valueOfLabel((String) shelfLine.get(j));
                    if (t.equals(Tile.Invalid)) { //check whether Tile is Invalid
                        throw new JsonBadParsingException("Error while generating Shelf from JSON : Tile is Invalid type");
                    }
                    tiles[i][j] = t;
                }
            }
            if (isNotValid()) {
                throw new JsonBadParsingException("Error while creating Shelf : input Shelf has not a valid configuration");
            }
        } catch (TileGenericException e) {
            throw new JsonBadParsingException("Error while creating Shelf : Tile type not found");
        }
    }

    //Observers
    /**
     * @return rows attribute
     */
    @Override
    public int getRows() {
        return rows;
    }
    /**
     * @return columns attribute
     */
    @Override
    public int getColumns() {
        return columns;
    }

    /**
     * Get tile in position pos
     * @param pos is the position object
     * @return Tile in position pos
     * @throws BadPositionException when getting position is out of bound
     */
    public Tile getTile(Position pos) throws BadPositionException{
        try {
            return getTile(pos.getRow(), pos.getColumn());
        } catch (NullPointerException e) {
            throw new ShelfRuntimeException("Error while getting Tile in Shelf : Position object is null pointer");
        }
    }
    /**
     * Get tile in position (row,column)
     * @param row is row index
     * @param column is column index
     * @return Tile in position (row,column)
     * @throws BadPositionException when getting coordinates are out of bound
     */
    public Tile getTile(int row, int column) throws BadPositionException {
        try {
            return tiles[row][column];
        } catch (IndexOutOfBoundsException e){
            throw new BadPositionException("Error while getting Tile in Shelf : Coordinates are beyond boundaries");
        }
    }

    /**
     * @return the max number of Empty tiles in a single column
     */
    public int getHighestColumn(){
        LongStream allColumns = LongStream.range(0, columns);
        Function<Long, Long> countEmptyTiles =
                x -> allTilesInColumn(Math.toIntExact(x))
                        .stream()
                        .filter(tile -> tile.equals(Tile.Empty))
                        .count();

        long result = allColumns
                .map(countEmptyTiles::apply)
                .max()
                .orElse(0);
        return Math.toIntExact(result);
    }

    /**
     * @return true if the shelf is full
     */
    public boolean isFull(){
        return getHighestColumn() == 0;
    }

    /**
     * Checks if the Shelf is valid according to the game rulels
     * @return true if valid
     */
    private boolean isNotValid(){
        boolean valid = true;
        boolean notEmptyFound;

        for(int j = 0; valid && j < columns; j++){
            notEmptyFound = false;
            for( int i = 0; valid && i < rows; i++){ //check if there are empty tiles between valid tiles ineach column
                if(tiles[i][j].equals(Tile.Empty)){
                    if(notEmptyFound)
                        valid = false;
                } else if (tiles[i][j].equals(Tile.Invalid)) {
                    valid = false;
                } else {
                    notEmptyFound = true;
                }
            }
        }
        return !valid;
    }

    /**
     * @return points count from group of adjacent Tiles in the board
     */
    public int countAdjacentPoints(){
        int count;
        int points = 0;

        boolean[][] visited = new boolean[rows][columns]; //matrix to check already visited positions while exploring recursively
        for(int i = 0; i < rows; i++){
            for(int j = 0; j < columns; j++){
                if (!visited[i][j]) {//if a cell hasn't been explored yet, then it starts looking for greatest size group containing this cell
                    count = exploreAdjacents(visited, new Position(i,j));
                    if(count >= 6)
                        points = points+8;
                    else if (count == 5)
                        points = points+5;
                    else if (count == 4)
                        points = points+3;
                    else if (count == 3)
                        points = points+2;
                }
            }
        }
        return points;
    }

    /**
     * recursive method to explore adjacent Tiles of same type
     * @param visited is matrix of already visited positions
     * @param pos is the position
     * @return the number of adjacent tiles of same type
     */
    public int exploreAdjacents(boolean[][] visited, Position pos) {
        int count = 0;
        if (!isValidTile(pos) || visited[pos.getRow()][pos.getColumn()]) // check if current tile is valid for point count
            return 0;
        visited[pos.getRow()][pos.getColumn()] = true;
        count = count + pos.neighbours().stream()
                .filter(x -> {
                    try {
                        return !isOutOfBounds(x.getRow(), x.getColumn()) && getTile(x).equals(getTile(pos)); //check neighbour x is a valid position
                    } catch (BadPositionException e) {
                        throw new ShelfRuntimeException("Error in the exploringAdjacents algorithm");
                    }
                })
                .mapToInt(x -> exploreAdjacents(visited, x))
                .sum();

        return count + 1;
    }
    /**
     * Tells if the coordinates are compatible with the shelf
     * @param row integer
     * @param column integer
     * @return True if the coordinates are valid
     */
    public boolean isOutOfBounds(int row, int column){
        return row < 0 || row >= getRows() || column < 0 || column >= getColumns();
    }
    /**
     * Tells if the position is Empty or Invalid
     * @param row integer
     * @param column integer
     * @return TRUE if the Tile is Empty or Invalid
     */
    public boolean isValidTile(int row, int column) {
        Tile tile = null;
        try {
            tile = getTile(row, column);
        } catch (BadPositionException e) {
            return false;
        }
        return !tile.equals(Tile.Empty) && !tile.equals(Tile.Invalid);
    }
    public boolean isValidTile(Position position){
        return isValidTile(position.getRow(),position.getColumn());
    }

    /**
     * Get all the Tiles in a specific column of the shelf
     * @param column a column of the shelf
     * @return an ArrayList containing all the Tiles in the selected column of the shelf
     */
    public ArrayList<Tile> allTilesInColumn(int column) {
        try {
            ArrayList<Tile> tiles = new ArrayList<>();
            for (int row = 0; row < getRows(); row++) {
                tiles.add(getTile(row, column));
            }
            return tiles;
        } catch (BadPositionException e) {
            throw new ShelfRuntimeException("Error while getting in allTilesInColumn for Shelf");
        }
    }
    /**
     * Get all the Tiles in a specific row of the shelf
     * @param row a row of the shelf
     * @return an ArrayList containing all the Tiles in the selected row of the shelf
     */
    public  ArrayList<Tile> allTilesInRow(int row) {
        try {
            ArrayList<Tile> tiles = new ArrayList<>();
            for (int column = 0; column < getColumns(); column++) {
                tiles.add(getTile(row, column));
            }
            return tiles;
        } catch (BadPositionException e){
            throw new ShelfRuntimeException("Error while getting in allTilesInRow for Shelf");
        }
    }
    /**
     * Get all the Tiles in a specific row of the shelf
     * @return all the Tiles in a specific row of the shelf
     */
    public ArrayList<Tile> getCorners(){
        ArrayList<Tile> corners = new ArrayList<>();
        int rows = getRows();
        int columns = getColumns();
        try {
            corners.add(getTile(0, 0));
            corners.add(getTile(0, columns - 1));
            corners.add(getTile(rows - 1, 0));
            corners.add(getTile(rows - 1, columns - 1));
            return corners;
        } catch (BadPositionException e){
            throw new ShelfRuntimeException("Error while getting in getCorners in Shelf");
        }
    }

    //Modifiers
    /**
     * Insert tile to selected column
     * @param tile is the tile object to insert
     * @param column is the selected column
     * @throws BadPositionException whether tile is Invalid or column is already full or selected column is invalid
     */
    public void insertTile(Tile tile, int column) throws BadPositionException{
        boolean isEmpty = false;
        try {
            if (!Tile.existsValue(tile))
                throw new ShelfRuntimeException("Error while inserting tile : Tile type doesn't exists");
            if (tile.equals(Tile.Empty)) { //if Tile is Empty then do nothing
                return;
            } else if (tile.equals(Tile.Invalid)) { //if Tile is Invalid then throw exception
                throw new BadPositionException("Error while inserting in Shelf : Tile is Invalid type");
            } else if (!tiles[0][column].equals(Tile.Empty)) { //if selected column has an element in first position aka (also known as) the column is full
                throw new BadPositionException("Error while inserting in Shelf : selected column is already full");
            }
            for (int i = tiles.length - 1; !isEmpty && i >= 0; i--) {// back-iterate the column to find first Empty cell to insert Tile
                if (tiles[i][column] == Tile.Empty) {
                    tiles[i][column] = tile;
                    isEmpty = true;
                }
            }
        } catch (IndexOutOfBoundsException e){
            throw new BadPositionException("Error while inserting in Shelf : selected column is not valid");
        }

        if (virtualShelf!=null)
            virtualShelf.onTileInsert(column, tile);  // Updating clients
    }

    /**
     * This puts a Tile in a specific position. This should never be called in game
     * @param tile
     * @param pos
     */
    public void putTile(Tile tile, Position pos) {
        if(!isOutOfBounds(pos.getRow(),pos.getColumn())){
            tiles[pos.getRow()][pos.getColumn()] = tile;
        }
    }

    //Others
    /**
     * compare two Shelf objects
     * @param o is the object to compare with
     * @return true if they have the same attributes, false otherwise
     */
    public boolean equals(Object o) {
        boolean sameShelf = true;
        if(o == null || this.getClass() != o.getClass()){ //check if they are not same class
            return false;
        } else if (this == o) { //check if it's the same object
            return true;
        }
        if(rows != ((Shelf) o).getRows() || columns != ((Shelf) o).getColumns()) //check if they have different dimensions
            return false;
        for (int i = 0; sameShelf && i < rows; i++) { //check they have same tiles in shelf
            for (int j = 0; sameShelf && j < columns; j++) {
                try {
                    if (!tiles[i][j].equals(((Shelf) o).getTile(i, j))) {
                        sameShelf = false;
                    }
                } catch (BadPositionException e) {
                    return false;
                }
            }
        }
        return sameShelf;
    }

    /**
     * Convert the matrix to a printable String
     * @return the string visual conversion of the matrix
     */
    @Override
    public String toString(){
        String str = "";
        String shelfTiles = "";
        String upperBorder = "╔═";
        String lowerBorder = "╚═";

        for(int row = 0; row < rows; row++){
            for(int col = 0; col < columns; col++){
                if(row == 0){
                    lowerBorder = lowerBorder.concat("═══");
                    upperBorder = upperBorder.concat("═══");
                }
                if(col == 0 ){
                    shelfTiles = shelfTiles.concat(Color.coloredString("║ ",Color.Yellow));
                }
                try {
                    shelfTiles = shelfTiles.concat(getTile(row,col).toColorFulString()+" ");
                } catch (BadPositionException e) {
                    //Will never be executed
                }
            }
            shelfTiles = shelfTiles.concat(Color.coloredString("║\n",Color.Yellow));
        }
        lowerBorder = lowerBorder.concat("╝\n");
        upperBorder = upperBorder.concat("╗\n");
        str = str.concat(
                Color.coloredString(upperBorder,Color.Yellow)+
                        shelfTiles+
                        Color.coloredString(lowerBorder,Color.Yellow));
        return str;
    }

    /**
     * generate hashcode from tiles matrix of shelf class
     * @return calculated hashcode
     */
    @Override
    public int hashCode(){
        return Arrays.deepHashCode(tiles); //deepHashCode is similar to HashCode but also applied to any sub-array of elements
    }
    /**
     * This method is used to save the status of the shelf with a json object.
     * @return JSONObject with status.
     */
    public JSONObject toJson() {
        JSONObject shelfJson = new JSONObject();  // Object to return

        // Saving final parameters
        shelfJson.put("numRows", Long.valueOf(rows));
        shelfJson.put("numColumns", Long.valueOf(columns));

        // Looping to save the matrix
        JSONArray shelfMatrix = new JSONArray();
        JSONArray shelfRowJson;  // Single row
        List<String> shelfRow;
        for (int row = 0; row < rows; row++) {  // Iterating over the rows
            shelfRowJson = new JSONArray();  // Will be added into the JSON matrix
            shelfRow = new ArrayList<>();  // To convert from [] -> collection

            for (int col = 0; col < columns; col++) {  // Iterating over the columns
                shelfRow.add(tiles[row][col].toString());
            }
            shelfRowJson.addAll(shelfRow);  // Converting from List to JSONArray
            shelfMatrix.add(shelfRowJson);
        }

        shelfJson.put("matrix", shelfMatrix);

        return shelfJson;
    }

    /**
     * This method is used to set the virtual shelf,
     * it'll be used to send updates when the
     * status changes.
     * @param virtualShelf virtual shelf object
     */
    public void setVirtualShelf(VirtualShelf virtualShelf) {
        this.virtualShelf = virtualShelf;
    }

    /**
     * This method is used to return the highest free position in a column
     * @param column int, index of the column
     * @return Position, the first free position in the column
     */
    public Position getFreePosition(int column) throws BadPositionException{
        for (int i = rows - 1; i >= 0; i--) {
            if (tiles[i][column] == Tile.Empty) {
                return new Position(i, column);
            }
        }
        throw new BadPositionException("Error while getting free position in Shelf : selected column is already full");
    }
}
