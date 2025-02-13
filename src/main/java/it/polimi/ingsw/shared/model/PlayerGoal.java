package it.polimi.ingsw.shared.model;

import it.polimi.ingsw.shared.Color;
import it.polimi.ingsw.shared.GameSettings;
import it.polimi.ingsw.shared.JsonBadParsingException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class loads a player's goal from a json file and
 * implements the method to calculate the amount of
 * points achieved by the player.
 */
public class PlayerGoal {
    private final List<GoalPosition> positionList;
    private final Map<Integer, Integer> pointsMap;
    private final int goalId;

    /**
     * This constructor is used when the goalId is known
     * @param jsonPath Path to the json file
     * @param goalId id of the goal to load
     * @throws JsonBadParsingException when a parsing error happens
     */
    public PlayerGoal(String jsonPath, int goalId) throws JsonBadParsingException {
        this.goalId = goalId;
        JSONParser jsonParser = new JSONParser();
        try (
                InputStream stream = this.getClass().getClassLoader().getResourceAsStream(jsonPath);
        ){
            if (stream==null)
                throw new JsonBadParsingException("Error while loading json: file not found");
            Reader reader = new InputStreamReader(stream);
            JSONObject obj = (JSONObject) jsonParser.parse(reader);
            if (obj == null) {
                throw new JsonBadParsingException("Error while parsing json");
            }

            this.positionList = buildPositionList(obj, goalId);
            this.pointsMap = buildPointsMap(obj);

        } catch (ParseException | ClassCastException e) {
            throw new JsonBadParsingException("Error while parsing json");
        } catch (IOException e ) {
            throw new JsonBadParsingException("Error while loading json: file not found");
        }
    }

    /**
     * This constructor is used when the goalId needs to be chosen randomly
     * @param jsonPath Path to the json file
     * @throws JsonBadParsingException when a parsing error happens
     */
    public PlayerGoal(String jsonPath) throws JsonBadParsingException {
        JSONParser jsonParser = new JSONParser();
        Random rand = new Random();
        try (
                InputStream stream = PlayerGoal.class.getClassLoader().getResourceAsStream(jsonPath)
        ){
            if(stream==null)
                throw new JsonBadParsingException("Error while loading json: file not found");
            Reader reader = new InputStreamReader(stream);
            JSONObject obj = (JSONObject) jsonParser.parse(reader);

            JSONObject goals = (JSONObject) (obj).get("goals");
            if (goals == null) {
                throw new JsonBadParsingException("Error while parsing json: goals not found");}

            goalId = rand.nextInt(goals.size());  // Choosing a random objective

            this.positionList = buildPositionList(obj, goalId);
            this.pointsMap = buildPointsMap(obj);

        } catch (IOException e) {
            throw new JsonBadParsingException("Error while loading json: file not found");
        } catch (ParseException | ClassCastException e) {
            throw new JsonBadParsingException("Error while parsing json");
        }

    }

    /**
     * This constructor is used when the goalId needs to be chosen randomly
     * @param jsonObject object from which to extract the info, this
     *                   object must have a goals field and a points field.
     * @throws JsonBadParsingException when a parsing error happens
     */
    public PlayerGoal(JSONObject jsonObject, int goalId) throws JsonBadParsingException {
        this.goalId = goalId;
            if (jsonObject == null) {
                throw new JsonBadParsingException("Error while parsing json");
            }

            this.positionList = buildPositionList(jsonObject, goalId);
            this.pointsMap = buildPointsMap(jsonObject);
    }

    /**
     * This method is used to get the amount of players goals available
     * in the provided json file.
     * @param jsonPath Path to the json file containing the goals
     * @return integer with the amount
     * @throws JsonBadParsingException when a parsing error happens
     */
    public static int playerGoalsAmount(String jsonPath) throws JsonBadParsingException {
        JSONParser jsonParser = new JSONParser();
        try (
                InputStream stream = PlayerGoal.class.getClassLoader().getResourceAsStream(jsonPath)
                ){
            if(stream==null)
                throw new JsonBadParsingException("Error while loading json: file not found");
            Reader reader = new InputStreamReader(stream);
            JSONObject obj = (JSONObject) jsonParser.parse(reader);

            return playerGoalsAmount(obj);
        } catch (IOException e) {
            throw new JsonBadParsingException("Error while loading json: file not found");
        } catch (ParseException | ClassCastException e) {
            throw new JsonBadParsingException("Error while parsing json");
        }
    }

    private static int playerGoalsAmount(JSONObject obj) throws JsonBadParsingException {
        try {
            JSONObject goals = (JSONObject) (obj).get("goals");
            if (goals == null) {
                throw new JsonBadParsingException("Error while parsing json: goals not found");}

            return goals.size();  // Choosing a random objective
        } catch (ClassCastException e) {
            throw new JsonBadParsingException("Error while parsing json");
        }
    }

    /**
     * This function builds an integer-integer map from a json object to
     * map the amount of tiles in the correct position to the points
     * achieved by the player.
     * @param objFromFile json object, must have a "points" field
     * @return Map<Interger, Integer> with the mapping
     * @throws JsonBadParsingException when a parsing error happens
     */
    private Map<Integer, Integer> buildPointsMap(JSONObject objFromFile) throws JsonBadParsingException {
        try {
            JSONObject jsonMap = (JSONObject) objFromFile.get("points");

            if (jsonMap==null) {
                throw new JsonBadParsingException("Error while parsing json: points not found");
            }

            Map<String, Long> stringLongMap = new HashMap<>(jsonMap);

            Map<Integer, Integer> result = new HashMap();

            // Mapping from <String, Long> to <Integer, Integer>
            for (Map.Entry<String, Long> entry : stringLongMap.entrySet()) {
                result.put(Integer.parseInt(entry.getKey()), Math.toIntExact(entry.getValue()));
            }

            return result;
        } catch (NullPointerException | ClassCastException e) {
            throw new JsonBadParsingException("Error while parsing json: points not found");
        }
    }

    /**
     * This function builds the single goal of the player from the json file.
     * @param objFromFile json object with all goals, must have a goals field
     * @param goalId id chosen by the caller
     * @return player's goal
     * @throws JsonBadParsingException when a parsing error happens.
     */
    private List<GoalPosition> buildPositionList(JSONObject objFromFile, int goalId) throws JsonBadParsingException {
        JSONObject goals = (JSONObject) objFromFile.get("goals");
        if (goals == null) {
            throw new JsonBadParsingException("Error while parsing json: goals not found");
        }

        JSONArray goal = (JSONArray) goals.get(String.valueOf(goalId));
        try {
            if (goal == null) {
                throw new JsonBadParsingException("Error while parsing json: goal by id not found");
            }
            return (List<GoalPosition>) goal
                    .stream()
                    .map(g -> {
                        try {
                            return parsePosition((JSONObject) g);
                        } catch (JsonBadParsingException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());
        } catch (RuntimeException e){
            throw new JsonBadParsingException("Error while parsing json: wrong position attributes");
        }
    }
    /**
     * This function parses a single json object and returns a GoalPosition
     * @param jsonBlock json object to be parsed
     * @return GoalPosition, parsing result
     * @throws JsonBadParsingException when a parsing error happens
     */
    private GoalPosition parsePosition(JSONObject jsonBlock) throws JsonBadParsingException {
        try {
            String tileStr = jsonBlock.get("tileName").toString();
            Tile tile = Tile.valueOf(tileStr);

            int posRow = Math.toIntExact((Long) jsonBlock.get("posRow"));
            int posCol = Math.toIntExact((Long) jsonBlock.get("posCol"));
            return new GoalPosition(new Position(posRow, posCol), tile);
        } catch (ClassCastException | IllegalArgumentException e) {
            throw new JsonBadParsingException("Error while parsing json: wrong position attributes");
        }
    }

    /**
     * This predicate checks if a single tile is in the correct position
     * @param shelf Player's shelf
     * @param pos GoalPosition to be checked
     * @return true/false
     */
    private boolean checkSinglePosition(Shelf shelf, GoalPosition pos) {
        try {
            return pos.getTile()
                    .equals(
                            shelf.getTile(pos.getPos())
                    );
        } catch (BadPositionException e) {
            return false;
        }

    }

    /**
     * This function maps the number of correct positions to the point achieved
     * @param c Correct position counted
     * @return Points achieved
     * @throws JsonBadParsingException when a parsing error happens
     */
    private int countToPoints(int c) throws JsonBadParsingException {
        if (c==0) return 0;
        try {
            return Math.toIntExact(
                    pointsMap.get(c)
            );
        } catch (Exception e) {
            throw new JsonBadParsingException("Error while parsing json: wrong point map");
        }
    }

    /**
     * This function uses the player's shelf and the properties of the
     * goal to calculate the amount of points achieved.
     * @param shelf player's shelf
     * @return points achieved
     * @throws JsonBadParsingException when a parsing error happens
     */
    public int check(Shelf shelf) throws JsonBadParsingException {
            int count = positionList
                    .stream()
                    .mapToInt(p -> checkSinglePosition(shelf, p) ? 1 : 0)
                    .sum();
            return countToPoints(count);
    }

    /**
     * This function returns the id of the goal.
     * Ids start from 0.
     * @return int goalId
     */
    public int getGoalId() { return goalId; }

    @Override
    public String toString() {
        Shelf shelf = new Shelf(GameSettings.shelfRows,GameSettings.shelfColumns);
        for(GoalPosition goalPosition : positionList){
            shelf.putTile(goalPosition.getTile(),goalPosition.getPos());
        }
        String strShelf = Color.coloredString("Personal Goal:\n",Color.Yellow) + shelf.toString();
        String strNumbers = Color.coloredString("Completed:",Color.Yellow);
        String strPoints = Color.coloredString( "Points:   ",Color.Yellow);
        for(Map.Entry<Integer,Integer> entry : pointsMap.entrySet()){
            int number = entry.getKey();
            int points = entry.getValue();
            strNumbers = strNumbers.concat(((number >= 10)?String.valueOf(number):" "+String.valueOf(number)) + " ");
            strPoints = strPoints.concat(((points >= 10)?String.valueOf(points):" "+String.valueOf(points)) + " ");
        }
        return strShelf.concat(strNumbers+"\n").concat(strPoints);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerGoal that = (PlayerGoal) o;
        return goalId == that.goalId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(goalId);
    }
}

class GoalPosition {
    private final Position pos;
    private final Tile tile;


    public Position getPos() {
        return pos;
    }

    public Tile getTile() {
        return tile;
    }

    public GoalPosition(Position pos, Tile tile) {
        this.pos = pos;
        this.tile = tile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GoalPosition that = (GoalPosition) o;
        return Objects.equals(pos, that.getPos()) && tile == that.getTile();
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, tile);
    }
}
