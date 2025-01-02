package eu.kukharev;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.*;

public class GameFieldManager {
    private final WindowManager windowManager;
    private int fieldSize;
    private final boolean manualPlacement;
    private static final int ORIGINAL_TILE_SIZE = 200;
    private static final int WINDOW_SIZE = 1000;
    private static final int CELL_SPACING = 0;

    private int[][] field;
    private boolean[][] visited;
    private Image tiles;
    private int playerX, playerY; // Координаты старта
    private int endX, endY;       // Координаты финиша
    private int tileSize;
    private GridPane grid;
    private final StackPane root = new StackPane();

    // Для статистики прохождения
    private final List<Integer> visitedValues = new ArrayList<>();
    private int moves = 0;

    // Флаги ручной расстановки
    private boolean startPlaced = false;
    private boolean endPlaced   = false;

    // Для таймера
    private final long startTime;

    // Элементы статистики
    private HBox statsBox;
    private ImageView movesView, sumView, scoreView, timerView;
    private Timeline timeline; // Для обновления таймера и статистики

    public GameFieldManager(WindowManager windowManager, int fieldSize, boolean manualPlacement) {
        this.windowManager = windowManager;
        this.fieldSize = fieldSize;
        this.manualPlacement = manualPlacement;
        this.startTime = System.currentTimeMillis();
    }

    public Scene createGameScene() {
        // Фон
        Image background = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/BG1.png")));
        ImageView backgroundView = new ImageView(background);
        backgroundView.setFitWidth(WINDOW_SIZE);
        backgroundView.setFitHeight(WINDOW_SIZE);

        // Сетка
        grid = new GridPane();
        grid.setPadding(new Insets(350, 300, 300, 300));
        grid.setHgap(CELL_SPACING);
        grid.setVgap(CELL_SPACING);

        // Загрузка тайлов
        tiles = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/tiles.png")));

        // Инициализация массива
        field = new int[fieldSize][fieldSize];
        visited = new boolean[fieldSize][fieldSize];

        calculateTileSize();

        // Если расстановка случайная, генерируем старт/конец и поле
        if (!manualPlacement) {
            generateRandomStartEnd();
            generateRandomField();
            // Чтобы можно было ходить, нужно считать,
            // что старт и конец уже "выставлены" (флаги)
            startPlaced = true;
            endPlaced   = true;
        }

        // Создаём панель статистики (moves, sum, score, timer)
        createStatsPanel();

        // Размещаем сетку + панель статистики в общем контейнере
        VBox container = new VBox(10, grid, statsBox);
        container.setAlignment(Pos.TOP_CENTER);

        // Инициальное обновление, чтобы отобразить нули
        updateStatsPanel();
        // Создаём ячейки

        for (int i = 0; i < fieldSize; i++) {
            for (int j = 0; j < fieldSize; j++) {
                StackPane cell = createCell(i, j);
                grid.add(cell, j, i);
            }
        }

        root.getChildren().addAll(backgroundView, container, grid);

        // Запускаем таймер, который каждую секунду обновляет статистику
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateStatsPanel()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        return new Scene(root, WINDOW_SIZE, WINDOW_SIZE);

    }

    private void createStatsPanel() {
        statsBox = new HBox(40);
        statsBox.setPadding(new Insets(20, 0, 0, 0));
        statsBox.setAlignment(Pos.CENTER);

        // Подписи (можно вывести в виде обычного текста)
        Label movesLabel = new Label("Steps:");
        Label sumLabel   = new Label("Sum:");
        Label scoreLabel = new Label("Score:");
        Label timeLabel  = new Label("Time:");

        // Картинки-цифры
        movesView = new ImageView();
        sumView   = new ImageView();
        scoreView = new ImageView();
        timerView = new ImageView();

        // Собираем каждый блок
        HBox movesBox = new HBox(5, movesLabel, movesView);
        HBox sumBox   = new HBox(5, sumLabel,   sumView);
        HBox scoreBox = new HBox(5, scoreLabel, scoreView);
        HBox timeBox  = new HBox(5, timeLabel,  timerView);

        // Добавляем все в statsBox
        statsBox.getChildren().addAll(movesBox, sumBox, scoreBox, timeBox);

        // Стили для наглядности (необязательно)
        movesLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16;");
        sumLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16;");
        scoreLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16;");
        timeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16;");
    }

    /** Обновление значений статистики (вызывается таймером и при каждом клике). */
    private void updateStatsPanel() {
        int sum = visitedValues.stream().mapToInt(Integer::intValue).sum();
        int steps = moves;

        // Если ещё 0 ходов — счёт 0
        double sc = (steps == 0) ? 0 : (double) sum / steps;

        int elapsedSec = (int) ((System.currentTimeMillis() - startTime) / 1000);

        // Переводим числа в «цифровые тайлы»
        movesView.setImage(createScoreImage(steps));
        sumView.setImage(createScoreImage(sum));
        scoreView.setImage(createScoreImage((int) Math.round(sc)));
        timerView.setImage(createScoreImage(elapsedSec));
    }

    /** Формируем картинку из цифровых тайлов для заданного числа. */
    private Image createScoreImage(int value) {
        HBox digitsBox = new HBox(5);
        digitsBox.setStyle("-fx-background-color: transparent;");
        String str = String.valueOf(value);

        for (char c : str.toCharArray()) {
            int digit = Character.getNumericValue(c);
            if (digit >= 0 && digit <= 9) {
                ImageView tileView = new ImageView(tiles);
                tileView.setViewport(getDigitViewport(digit));
                tileView.setFitWidth(tileSize);
                tileView.setFitHeight(tileSize);
                digitsBox.getChildren().add(tileView);
            }
        }
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        return digitsBox.snapshot(params, null);
    }

    // Генерация случайных старт/конец
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

    // Генерация случайных значений (кроме старт/конца)
    private void generateRandomField() {
        Random random = new Random();
        for (int i = 0; i < fieldSize; i++) {
            for (int j = 0; j < fieldSize; j++) {
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
        // Нельзя ставить конец прямо рядом (dx=0/dy=0) — условие из старого кода
        return (dx >= 2 || dy >= 2 || (dx == 1 && dy == 1));
    }

    private void calculateTileSize() {
        int maxDim = Math.max(fieldSize, fieldSize);
        tileSize = Math.min((WINDOW_SIZE - 600) / maxDim, ORIGINAL_TILE_SIZE);
    }

    private StackPane createCell(int x, int y) {
        StackPane cell = new StackPane();
        ImageView imageView = new ImageView(tiles);

        imageView.setViewport(getFieldViewport(field[x][y]));
        imageView.setFitWidth(tileSize);
        imageView.setFitHeight(tileSize);

        cell.getChildren().add(imageView);
        cell.setOnMouseClicked(event -> handleCellClick(event, x, y));
        return cell;
    }

    // Возвращаем нужный кусок тайлсета для клетки поля
    private Rectangle2D getFieldViewport(int value) {
        // value: 1..9 => индексы 0..8
        // -1 => 9 (start), -2 => 11 (end), 0 => 10 (пустая)
        int index;
        switch (value) {
            case -1:  index = 9;  break; // Start
            case -2:  index = 11; break; // End
            case 0:   index = 10; break; // Пустая
            default:  index = value - 1;  break;
        }
        double x = index * ORIGINAL_TILE_SIZE;
        return new Rectangle2D(x, 0, ORIGINAL_TILE_SIZE, ORIGINAL_TILE_SIZE);
    }

    /**
     * Возвращаем нужный кусок тайлсета для отображения "цифры" в очках и таймере.
     * В нашем спрайте цифра '1' соответствует индексу 0, '2'—1, ..., '9'—8.
     * Если придёт 0, то подставим что-нибудь или пропустим (если нужно).
     */
    private Rectangle2D getDigitViewport(int digit) {
        // digit=1 => index=0, digit=2 => index=1, ..., digit=9 => 8, digit=0 => 9, можно скорректировать
        // Ниже вариант: 1=0, 2=1, ..., 9=8, 0=9 (или по-другому).
        int index = (digit == 0) ? 9 : (digit - 1);
        double x = index * ORIGINAL_TILE_SIZE;
        return new Rectangle2D(x, 0, ORIGINAL_TILE_SIZE, ORIGINAL_TILE_SIZE);
    }

    private void handleCellClick(MouseEvent event, int x, int y) {
        // Если ручная расстановка: сначала ставим старт, потом конец
        if (manualPlacement && !startPlaced) {
            field[x][y] = -1;
            playerX = x;
            playerY = y;
            startPlaced = true;
            updateGrid();
            updateStatsPanel();
            return;
        }
        if (manualPlacement && startPlaced && !endPlaced) {
            if (!isValidEndPosition(playerX, playerY, x, y)) return;
            field[x][y] = -2;
            endX = x;
            endY = y;
            endPlaced = true;

            generateRandomField();
            updateGrid();
            updateStatsPanel();
            return;
        }

        // Основная логика передвижения (только если и старт, и конец уже на поле)
        if (startPlaced && endPlaced) {
            if (isNeighbor(x, y) && !visited[x][y]) {
                visited[playerX][playerY] = true;
                visitedValues.add(field[x][y]);
                moves++;

                if (field[x][y] == -2) {
                    // Перед тем как выйти, обновим панель (движение +1)
                    updateStatsPanel();
                    endGame();
                    return;
                }

                field[playerX][playerY] = 0;
                playerX = x;
                playerY = y;
                field[playerX][playerY] = -1;

                updateGrid();
                updateStatsPanel();
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
        // Остановим таймер
        if (timeline != null) {
            timeline.stop();
        }
        int sum = visitedValues.stream().mapToInt(Integer::intValue).sum();
        double finalScore = (moves == 0) ? 0 : (double) sum / moves;
        double bestScore = computeBestScore();
        long endTime = System.currentTimeMillis();
        int totalTimeSec = (int)((endTime - startTime) / 1000);

        showEndScreen(finalScore, bestScore, totalTimeSec);
    }

    /**
     * Находим путь с минимальной суммой клеток (D’эйкстра).
     * Итоговый счёт — (сумма) / (шаги).
     */
    private double computeBestScore() {
        // Если вдруг старт или финиш не выставлены, возвращаем заглушку
        if (!startPlaced || !endPlaced) return 1.0;

        int n = fieldSize;
        int[][] bestCost = new int[n][n];   // накопительная сумма
        int[][] bestSteps = new int[n][n];  // сколько шагов потребовалось

        for (int i = 0; i < n; i++) {
            Arrays.fill(bestCost[i], Integer.MAX_VALUE);
            Arrays.fill(bestSteps[i], Integer.MAX_VALUE);
        }

        // Начальная точка
        bestCost[playerX][playerY] = Math.max(field[playerX][playerY], 0);
        bestSteps[playerX][playerY] = 1; // считаем, что мы уже стоим на старте

        // Очередь
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a.cost));
        pq.offer(new Node(playerX, playerY, bestCost[playerX][playerY], 1));

        // смещения
        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};

        while (!pq.isEmpty()) {
            Node cur = pq.poll();
            int cx = cur.x;
            int cy = cur.y;
            int costSoFar = cur.cost;
            int stepsSoFar = cur.steps;

            // Если мы уже пришли к финишу
            if (cx == endX && cy == endY) {
                // Считаем Score = costSoFar / stepsSoFar
                return (double) costSoFar / stepsSoFar;
            }

            // Если данные не актуальны, пропустим
            if (costSoFar > bestCost[cx][cy]) continue;

            for (int i = 0; i < 4; i++) {
                int nx = cx + dx[i];
                int ny = cy + dy[i];
                if (nx < 0 || nx >= n || ny < 0 || ny >= n) continue;

                // Стоимость текущей клетки (если -2, считаем как 0 или 0+?)
                int cellVal = field[nx][ny];
                if (cellVal < 0) cellVal = 0; // если это старт / конец — пусть будет 0
                int newCost  = costSoFar + cellVal;
                int newSteps = stepsSoFar + 1;

                // Если улучшили результат
                if (newCost < bestCost[nx][ny] ||
                        (newCost == bestCost[nx][ny] && newSteps < bestSteps[nx][ny]))
                {
                    bestCost[nx][ny] = newCost;
                    bestSteps[nx][ny] = newSteps;
                    pq.offer(new Node(nx, ny, newCost, newSteps));
                }
            }
        }
        // Если не нашли путь, возвращаем 9999
        return 9999.0;
    }

    // Узел для очереди
    private static class Node {
        int x, y, cost, steps;
        Node(int x, int y, int cost, int steps) {
            this.x = x;
            this.y = y;
            this.cost = cost;
            this.steps = steps;
        }
    }

    /**
     * Отображение экрана завершения игры.
     * Показываем наш счёт, лучший счёт, время и надпись WIN/LOSE + кнопки.
     */
    private void showEndScreen(double finalScore, double bestScore, int totalTimeSec) {
        // Задний фон: BG1.png (исправлено согласно пункту 7)
        Image background = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/BG1.png")));
        ImageView backgroundView = new ImageView(background);
        backgroundView.setFitWidth(WINDOW_SIZE);
        backgroundView.setFitHeight(WINDOW_SIZE);

        // Проверяем, выиграли ли мы (если наш счёт <= оптимального)
        boolean isWin = (Double.compare(finalScore, bestScore) <= 0);

        // Накладываем поверх WIN или LOSE
        ImageView resultView = new ImageView(new Image(
                Objects.requireNonNull(
                        getClass().getResourceAsStream(isWin ? "/WIN.png" : "/LOST.png")
                )));
        resultView.setFitWidth(600);
        resultView.setFitHeight(300);
        resultView.setTranslateY(-200);

        // Отображение счёта игрока
        ImageView playerScoreView = createScoreView((int) Math.round(finalScore));
        playerScoreView.setTranslateY(-20); // немного вниз

        // Отображение лучшего счёта
        ImageView bestScoreView = createScoreView((int) Math.round(bestScore));
        bestScoreView.setTranslateY(80);

        // Отображение времени
        ImageView timeView = createScoreView(totalTimeSec);
        timeView.setTranslateY(180);

        // Кнопки
        Button toMenuButton = createImageButton("/toMenu.png");
        toMenuButton.setOnAction(event -> goToMenu());

        Button betterWayButton = createImageButton("/bestWay.png");
        betterWayButton.setOnAction(event -> {
            highlightBestPath();
            updateGrid();
        });

        VBox buttons = new VBox(20, toMenuButton, betterWayButton);
        buttons.setTranslateY(300);

        // Собираем все элементы
        root.getChildren().clear();
        root.getChildren().addAll(backgroundView, resultView, playerScoreView, bestScoreView, timeView, buttons);
    }

    private Button createImageButton(String texturePath) {
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(texturePath)));
        ImageView imageView = new ImageView(image);
        Button button = new Button();
        button.setGraphic(imageView);
        button.setStyle("-fx-background-color: transparent;");
        return button;
    }

    /**
     * Создание изображения с набором цифр (для счёта, таймера).
     * Использует getDigitViewport(digit) вместо getFieldViewport.
     */
    private ImageView createScoreView(int scoreValue) {
        HBox scoreTiles = new HBox(5);
        scoreTiles.setStyle("-fx-background-color: transparent;");

        // Разбиваем число на цифры
        String str = String.valueOf(scoreValue);

        for (char c : str.toCharArray()) {
            int digit = Character.getNumericValue(c);
            if (digit < 0 || digit > 9) continue; // защита

            ImageView tileView = new ImageView(tiles);
            tileView.setViewport(getDigitViewport(digit));
            tileView.setFitWidth(tileSize);
            tileView.setFitHeight(tileSize);
            scoreTiles.getChildren().add(tileView);
        }

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);

        return new ImageView(scoreTiles.snapshot(params, null));
    }

    private void goToMenu() {
        windowManager.initialize(windowManager.stage);
    }

    /**
     * Подсветка потенциально лучшего пути (упрощённо).
     * Можно адаптировать, чтобы реально отрисовывать маршрут D’эйкстры.
     */
    private void highlightBestPath() {
        // Для простоты выделим все непосещённые клетки (>=1).
        // При желании можно восстановить путь из массива bestCost/bestSteps.
        for (int i = 0; i < fieldSize; i++) {
            for (int j = 0; j < fieldSize; j++) {
                if (!visited[i][j] && field[i][j] > 0) {
                    StackPane cell = new StackPane();
                    cell.setStyle("-fx-background-color: rgba(0, 255, 0, 0.3);");
                    grid.add(cell, j, i);
                }
            }
        }
    }
}