package eu.kukharev;

import java.util.ArrayList;
import java.util.List;

public class GameState {
    public int fieldSize;
    public int playerX, playerY;
    public int endX, endY;
    public int moves;
    public int[][] field;
    public boolean[][] visited;
    public List<Integer> visitedValues = new ArrayList<>();
}
