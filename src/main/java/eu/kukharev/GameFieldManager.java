package eu.kukharev;

import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.*;

public class GameFieldManager {
    private final WindowManager windowManager;
    private final int fieldSize;        // Размер поля
    private final boolean manualPlacement; // Ручная расстановка или случайная
    private static final int ORIGINAL_TILE_SIZE = 200;
    private static final int WINDOW_SIZE = 1000;
    private static final int CELL_SPACING = 0;

    private int[][] field;
    private boolean[][] visited;
    private Image tiles;
    private int playerX, playerY; // Старт
    private int endX, endY;       // Финиш
    private int tileSize;
    private GridPane grid;
    private final StackPane root = new StackPane();

    // Для статистики прохождения
    private final List<Integer> visitedValues = new ArrayList<>();
    private int moves = 0;

    // Флаги для ручной расстановки
    private boolean startPlaced = false;
    private boolean endPlaced = false;

    public GameFieldManager(WindowManager windowManager, int fieldSize, boolean manualPlacement) {
        this.windowManager = windowManager;
        this.fieldSize = fieldSize;
        this.manualPlacement = manualPlacement;
    }

    public Scene createGameScene() {
        // Фон
        Image background = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/BG1.png")));
        ImageView backgroundView = new ImageView(background);
        backgroundView.setFitWidth(WINDOW_SIZE);
        backgroundView.setFitHeight(WINDOW_SIZE);

        // Сетка для поля
        grid = new GridPane();
        grid.setPadding(new Insets(350, 300, 300, 300));
        grid.setHgap(CELL_SPACING);
        grid.setVgap(CELL_SPACING);

        // Загрузка тайлсета
        tiles = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/tiles.png")));

        // Инициализация массива
        field = new int[fieldSize][fieldSize];
        visited = new boolean[fieldSize][fieldSize];

        // Расчет динамического размера клетки
        calculateTileSize();

        // Если выбран случайный способ, сразу генерируем старт/финиш и поле
        if (!manualPlacement) {
            generateRandomStartEnd();
            generateRandomField();
        }

        // Создаём ячейки
        for (int i = 0; i < fieldSize; i++) {
            for (int j = 0; j < fieldSize; j++) {
                StackPane cell = createCell(i, j);
                grid.add(cell, j, i);
            }
        }

        root.getChildren().addAll(backgroundView, grid);
        return new Scene(root, WINDOW_SIZE, WINDOW_SIZE);
    }

    // Генерация случайных старт и конец
    private void generateRandomStartEnd() {
        Random random = new Random();
        playerX = random.nextInt(fieldSize);
        playerY = random.nextInt(fieldSize);
        field[playerX][playerY] = -1; // Start

        do {
            endX = random.nextInt(fieldSize);
            endY = random.nextInt(fieldSize);
        } while (!isValidEndPosition(playerX, playerY, endX, endY));

        field[endX][endY] = -2; // End
    }

    // Генерация случайных значений клеток
    private void generateRandomField() {
        Random random = new Random();
        for (int i = 0; i < fieldSize; i++) {
            for (int j = 0; j < fieldSize; j++) {
                // Если не старт и не конец, кладем рандом
                if (field[i][j] != -1 && field[i][j] != -2) {
                    field[i][j] = random.nextInt(9) + 1;
                }
            }
        }
    }

    // Проверка валидности финиша
    private boolean isValidEndPosition(int sx, int sy, int ex, int ey) {
        if (sx == ex && sy == ey) return false;
        int dx = Math.abs(sx - ex);
        int dy = Math.abs(sy - ey);
        // Считаем, что нельзя ставить конец рядом вплотную
        return (dx >= 2 || dy >= 2 || (dx == 1 && dy == 1));
    }

    private void calculateTileSize() {
        int maxDim = Math.max(fieldSize, fieldSize);
        tileSize = Math.min((WINDOW_SIZE - 600) / maxDim, ORIGINAL_TILE_SIZE);
    }

    private StackPane createCell(int x, int y) {
        StackPane cell = new StackPane();
        ImageView imageView = new ImageView(tiles);
        imageView.setViewport(getViewport(field[x][y]));
        imageView.setFitWidth(tileSize);
        imageView.setFitHeight(tileSize);

        cell.getChildren().add(imageView);

        // Обработка клика по ячейке
        cell.setOnMouseClicked(event -> handleCellClick(event, x, y));
        return cell;
    }

    // Возвращаем нужный фрагмент тайлсета
    private Rectangle2D getViewport(int value) {
        int index;
        if (value == -1) index = 9;     // Start
        else if (value == -2) index = 11;  // End
        else if (value == 0) index = 10;   // Пустая
        else index = value - 1;            // Цифра 1..9

        double x = index * ORIGINAL_TILE_SIZE;
        return new Rectangle2D(x, 0, ORIGINAL_TILE_SIZE, ORIGINAL_TILE_SIZE);
    }

    private void handleCellClick(MouseEvent event, int x, int y) {
        // Если ручной режим и старт не выставлен
        if (manualPlacement && !startPlaced) {
            field[x][y] = -1; // ставим старт
            playerX = x;
            playerY = y;
            startPlaced = true;
            updateGrid();
            return;
        }
        // Если ручной режим, старт уже есть, но нет конца
        if (manualPlacement && startPlaced && !endPlaced) {
            // Ставим конец
            // Проверим, можно ли здесь ставить
            if (!isValidEndPosition(playerX, playerY, x, y)) return;
            field[x][y] = -2;
            endX = x;
            endY = y;
            endPlaced = true;
            // Как только оба выставлены, генерируем случайные числа на остальные клетки
            generateRandomField();
            updateGrid();
            return;
        }

        // Далее — логика хода игрока
        // Клик принимаем только если и старт и конец уже выставлены
        if (startPlaced && endPlaced) {
            if (isNeighbor(x, y) && !visited[x][y]) {
                visited[playerX][playerY] = true;
                visitedValues.add(field[x][y]);
                moves++;

                int sum = visitedValues.stream().mapToInt(Integer::intValue).sum();
                System.out.println("Moves: " + moves + ", Sum: " + sum + " Score: " + (double) sum / moves);

                if (field[x][y] == -2) {
                    endGame();
                    return;
                }

                // Ставим прежнюю клетку пустой
                field[playerX][playerY] = 0;

                playerX = x;
                playerY = y;
                field[playerX][playerY] = -1;

                updateGrid();
            }
        }
    }

    private boolean isNeighbor(int x, int y) {
        return (Math.abs(playerX - x) == 1 && playerY == y)
                || (Math.abs(playerY - y) == 1 && playerX == x);
    }

    private void updateGrid() {
        grid.getChildren().clear();
        for (int i = 0; i < fieldSize; i++) {
            for (int j = 0; j < fieldSize; j++) {
                StackPane cell = createCell(i, j);
                grid.add(cell, j, i);
            }
        }
    }

    private void endGame() {
        int sum = visitedValues.stream().mapToInt(Integer::intValue).sum();
        double score = (double) sum / moves;
        double bestScore = findBestScore(); // заглушка

        showWinMenu(score, bestScore);
    }

    private double findBestScore() {
        // Заглушка для демонстрации
        return 1.0;
    }

    private void showWinMenu(double score, double bestScore) {
        Image background = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/BG2.png")));
        ImageView backgroundView = new ImageView(background);
        backgroundView.setFitWidth(WINDOW_SIZE);
        backgroundView.setFitHeight(WINDOW_SIZE);

        // Отображение счета игрока
        ImageView playerScoreView = createScoreView((int) Math.round(score));
        playerScoreView.setTranslateY(-100);

        // Лучший счёт
        ImageView bestScoreView = createScoreView((int) Math.round(bestScore));
        bestScoreView.setTranslateY(50);

        // Кнопки
        Button toMenuButton = createImageButton("/toMenu.png");
        toMenuButton.setOnAction(event -> goToMenu());

        Button betterWayButton = createImageButton("/bestWay.png");
        betterWayButton.setOnAction(event -> {
            highlightBestPath();
            updateGrid();
        });

        VBox buttons = new VBox(20, toMenuButton, betterWayButton);
        buttons.setTranslateY(200);

        root.getChildren().clear();
        root.getChildren().addAll(backgroundView, playerScoreView, bestScoreView, buttons);
    }

    private Button createImageButton(String texturePath) {
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(texturePath)));
        ImageView imageView = new ImageView(image);
        Button button = new Button();
        button.setGraphic(imageView);
        button.setStyle("-fx-background-color: transparent;");
        return button;
    }

    private ImageView createScoreView(int score) {
        HBox scoreTiles = new HBox(5);
        scoreTiles.setStyle("-fx-background-color: transparent;");
        String scoreStr = String.valueOf(score);
        for (char digit : scoreStr.toCharArray()) {
            int tileValue = Character.getNumericValue(digit);
            if (tileValue >= 0 && tileValue <= 9) {
                ImageView tileView = new ImageView(tiles);
                tileView.setViewport(getViewport(tileValue));
                tileView.setFitWidth(tileSize);
                tileView.setFitHeight(tileSize);
                scoreTiles.getChildren().add(tileView);
            }
        }
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        return new ImageView(scoreTiles.snapshot(params, null));
    }

    private void goToMenu() {
        windowManager.initialize(windowManager.stage);
    }

    // Подсвечиваем кратчайший путь (BFS/Dijkstra)
    private void highlightBestPath() {
        // Упрощённая демонстрация
        // Здесь можно использовать реальную логику поиска пути (Dijkstra)
        // Для примера выставим всем непосещенным клеткам полупрозрачный цвет
        for (int i = 0; i < fieldSize; i++) {
            for (int j = 0; j < fieldSize; j++) {
                if (!visited[i][j] && field[i][j] >= 1) {
                    // Делаем небольшую подсветку
                    StackPane cell = new StackPane();
                    cell.setStyle("-fx-background-color: rgba(0, 255, 0, 0.3);");
                    grid.add(cell, j, i);
                }
            }
        }
    }
}
