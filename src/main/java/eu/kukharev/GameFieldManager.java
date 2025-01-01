package eu.kukharev;

import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import eu.kukharev.WindowManager;
import javafx.stage.Stage;

public class GameFieldManager {
    private static final int ORIGINAL_TILE_SIZE = 200; // Исходный размер клетки
    private static final int WINDOW_SIZE = 1000;       // Размер окна
    private static final int CELL_SPACING = 0;        // Расстояние между клетками

    private int[][] field;
    private boolean[][] visited; // Массив для отслеживания посещенных клеток
    private Image tiles;
    private int playerX, playerY; // Координаты игрока
    private int tileSize;
    private GridPane grid;
    StackPane root = new StackPane();

    private List<Integer> visitedValues = new ArrayList<>(); // Список значений посещенных клеток
    private int moves = 0; // Количество ходов

    public Scene createGameScene() {
        Image background = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/BG1.png")));
        ImageView backgroundView = new ImageView(background);
        backgroundView.setFitWidth(WINDOW_SIZE);
        backgroundView.setFitHeight(WINDOW_SIZE);

        grid = new GridPane();
        grid.setPadding(new Insets(350, 300, 300, 300));

        tiles = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/tiles.png")));
        generateField();
        calculateTileSize();

        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[i].length; j++) {
                StackPane cell = createCell(i, j);
                grid.add(cell, j, i);
            }
        }

        root.getChildren().addAll(backgroundView, grid);
        return new Scene(root, WINDOW_SIZE, WINDOW_SIZE);
    }

    private void generateField() {
        Random random = new Random();
        int size = random.nextInt(8) + 2; // Размер от 2 до 9
        field = new int[size][size];
        visited = new boolean[size][size];

        // Генерация случайных значений клеток
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                field[i][j] = random.nextInt(9) + 1; // Числа от 1 до 9
            }
        }

        // Установка стартовой и конечной клетки
        playerX = random.nextInt(size);
        playerY = random.nextInt(size);
        field[playerX][playerY] = -1; // Start

        int endX, endY;
        do {
            endX = random.nextInt(size);
            endY = random.nextInt(size);
        } while (endX == playerX && endY == playerY);
        field[endX][endY] = -2; // End
    }

    private void calculateTileSize() {
        int maxFieldSize = Math.max(field.length, field[0].length);
        tileSize = Math.min((WINDOW_SIZE - 600) / maxFieldSize, ORIGINAL_TILE_SIZE);
    }

    private StackPane createCell(int x, int y) {
        StackPane cell = new StackPane();
        ImageView imageView = new ImageView(tiles);
        imageView.setViewport(getViewport(field[x][y]));
        imageView.setFitWidth(tileSize);
        imageView.setFitHeight(tileSize);

        cell.getChildren().add(imageView);
        cell.setOnMouseClicked(event -> handleCellClick(event, x, y));
        return cell;
    }

    private Rectangle2D getViewport(int value) {
        int index = switch (value) {
            case -1 -> 9; // Start (игрок)
            case -2 -> 11; // End
            case 0 -> 10;   // Пустая клетка
            default -> value - 1; // Цифры 1-9
        };
        double x = index * ORIGINAL_TILE_SIZE;
        return new Rectangle2D(x, 0, ORIGINAL_TILE_SIZE, ORIGINAL_TILE_SIZE);
    }

    /**
     * Обрабатывает клик по клетке.
     *
     * @param event Событие клика
     * @param x     Координата X клетки
     * @param y     Координата Y клетки
     */
    private void handleCellClick(MouseEvent event, int x, int y) {
        if (isNeighbor(x, y) && !isVisited(x, y)) {
            visited[playerX][playerY] = true;

            visitedValues.add(field[x][y]);

            moves++;
            int sum = visitedValues.stream().mapToInt(Integer::intValue).sum();
            System.out.println("Moves: " + moves + ", Sum: " + sum + " score: " + sum / moves);

            if (field[x][y] == -2) {
                endGame();
                return;
            }

            field[playerX][playerY] = 0;

            playerX = x;
            playerY = y;
            field[playerX][playerY] = -1;

            updateGrid();
        }
    }


    private void updateGrid() {
        grid.getChildren().clear();

        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[i].length; j++) {
                StackPane cell = createCell(i, j);
                grid.add(cell, j, i);
            }
        }
    }

    private boolean isNeighbor(int x, int y) {
        return (Math.abs(playerX - x) == 1 && playerY == y) || (Math.abs(playerY - y) == 1 && playerX == x);
    }

    private boolean isVisited(int x, int y) {
        return visited[x][y];
    }

    private void endGame() {
        int sum = visitedValues.stream().mapToInt(Integer::intValue).sum();
        double score = (double) sum / moves;
        System.out.println("Game Over! Score: " + score);
        double bestScore = findBestScore();
        winMenu(root, score, bestScore);
    }

    public double findBestScore() {
        double bestScore = 1;
        return bestScore;
    }

    private void winMenu(StackPane root, double score, double bestScore) {
        Image background = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/BG2.png")));
        ImageView backgroundView = new ImageView(background);
        backgroundView.setFitWidth(1000);
        backgroundView.setFitHeight(1000);
        Image RESOULT;
        if (score == bestScore) {
            RESOULT = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/WIN.png")));
        } else {
            RESOULT = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/LOST.png")));
        }
        ImageView RESOULTView = new ImageView(RESOULT);
        RESOULTView.setFitWidth(1000);
        RESOULTView.setFitHeight(1000);
        root.getChildren().clear();
        root.getChildren().add(backgroundView);
        root.getChildren().add(RESOULTView);

    }
}
