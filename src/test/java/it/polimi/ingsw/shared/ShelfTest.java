package it.polimi.ingsw.shared;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class ShelfTest {
    final int rows = 6;
    final int columns = 5;
    final String value = "Cat";
    @Test
    void insert_test() {
        Shelf s = new Shelf(rows, columns);
        for (int i = 0; i < rows; i++) { // check it initializes the object correctly
            for (int j = 0; j < columns; j++) {
                assertEquals(s.getTile(new Position(i, j)), Tile.Empty);
            }
        }
        for (int j = 0; j < columns - 1; j++) {
            for (int i = 0; i < rows - 2; i++) {
                s.insertTile(Tile.valueOf(value), j);
                assertEquals(s.getTile(new Position(rows - i - 1, j)), Tile.valueOf(value)); // check insert work correctly
            }
        }
        assertEquals(s.getHighestColumn(), rows); //check getHighestColumn doesn't change in value after filling all columns except for last one
        for (int i = 0; i < rows - 2; i++) {
            s.insertTile(Tile.valueOf(value), columns - 1);
            assertEquals(s.getHighestColumn(), rows - i - 1); //check getHighestColumn change in value as I fill last column
        }
        System.out.println(s);
        System.out.println(s.hashCode());
    }
    @Test
    void shelf_equals_test(){
        Shelf s1 = new Shelf(rows, columns);
        Shelf s2 = new Shelf(rows, columns);
        for(int j = 0; j< columns-1; j++){
            s1.insertTile(Tile.valueOf("Cat"), j);
            s2.insertTile(Tile.valueOf("Cat"), j);
        }
        s1.insertTile(Tile.valueOf("Book"), 0);
        System.out.println("s1_1:");
        System.out.println(s1);
        System.out.println("s2_1:");
        System.out.println(s2);
        assert(!s1.equals(s2)); //check they are different
        s2.insertTile(Tile.valueOf("Book"), 0);
        System.out.println("s1_2:");
        System.out.println(s1);
        System.out.println("s2_2:");
        System.out.println(s2);
        assert(s1.equals(s2)); //check they are the same
    }
    @Test
    void ShelfJsonInit(){

    }
}
