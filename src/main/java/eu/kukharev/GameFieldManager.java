package eu.kukharev;

import javafx.scene.Scene;
import javafx.scene.layout.GridPane;

import java.util.Random;

public class GameFieldManager {
    private int[][] field;

    public Scene createGameScene() {
        GridPane grid = new GridPane();
        generateField();

        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[i].length; j++) {
                grid.add(new Cell(field[i][j]).getNode(), j, i);
            }
        }

        return new Scene(grid, 804, 804);
    }

    private void generateField() {
        Random random = new Random();
        int size = random.nextInt(8) + 2; // Размер от 2 до 9
        field = new int[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                field[i][j] = random.nextInt(9) + 1; // Числа от 1 до 9
            }
        }

        // Добавление стартовой и конечной клеток
        field[0][0] = -1; // Start
        field[size - 1][size - 1] = -2; // End
    }
}
