package eu.kukharev;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
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

/**
 * Класс, управляющий игровым полем и логикой игры.
 */
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
    private int playerX, playerY;   // Текущее положение игрока
    private int startX, startY;     // Изначальная точка старта (не меняется)
    private int endX, endY;         // Координаты финиша
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
    private Timeline blinkTimeline; // отдельная анимация для мигания

    // Элементы статистики
    private VBox statsBox;
    private ImageView movesView, sumView, scoreView, timerView;
    private Timeline timeline; // Для обновления таймера и статистики
    private double bestScore = Double.MAX_VALUE;

    // Список координат лучшего пути
    private List<int[]> bestPath = new ArrayList<>();

    // Флаг, чтобы при показе лучшего пути убрать клики по клеткам
    private boolean bestPathMode = false;

    // Сохраним ссылки на элементы "финального" экрана (WIN/LOSE),
    // чтобы можно было вернуться из режима показа пути
    private List<Node> endScreenNodesBackup = new ArrayList<>();

    /**
     * Конструктор для нового поля (с опцией ручной или автоматической расстановки).
     */
    public GameFieldManager(WindowManager windowManager, int fieldSize, boolean manualPlacement) {
        this.windowManager = windowManager;
        this.fieldSize = fieldSize;
        this.manualPlacement = manualPlacement;

        this.field = new int[fieldSize][fieldSize];
        this.visited = new boolean[fieldSize][fieldSize];
        this.startTime = System.currentTimeMillis();

        // Новые поля для запоминания исходной точки старта
        this.startX = -1;
        this.startY = -1;
    }

    /**
     * Конструктор для **загрузки** готового состояния из GameState.
     */
    public GameFieldManager(WindowManager windowManager, GameState loadedState) {
        this.windowManager = windowManager;
        this.fieldSize = loadedState.fieldSize;
        this.field     = loadedState.field;
        this.visited   = loadedState.visited;
        this.playerX   = loadedState.playerX;
        this.playerY   = loadedState.playerY;
        this.endX      = loadedState.endX;
        this.endY      = loadedState.endY;
        this.moves     = loadedState.moves;
        this.visitedValues.addAll(loadedState.visitedValues);

        // Поскольку поле уже готовое, ставим manualPlacement = false
        this.manualPlacement = false;

        // Проверяем, действительно ли в поле -1 (старт) и -2 (финиш)
        if (isValidCoord(playerX, playerY) && field[playerX][playerY] == -1) {
            startPlaced = true;
            // Запомним исходный старт
            this.startX = playerX;
            this.startY = playerY;
        }
        if (isValidCoord(endX, endY) && field[endX][endY] == -2) {
            endPlaced = true;
        }
        this.startTime = System.currentTimeMillis();
    }

    /**
     * Создаёт игровую сцену (поле и панель статистики).
     */
    public Scene createGameScene() {
        // Фон
        Image background = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/BG1.png")));
        ImageView backgroundView = new ImageView(background);
        backgroundView.setFitWidth(WINDOW_SIZE);
        backgroundView.setFitHeight(WINDOW_SIZE);

        grid = new GridPane();
        grid.setPadding(new Insets(350, 100, 300, 350));
        grid.setHgap(CELL_SPACING);
        grid.setVgap(CELL_SPACING);

        tiles = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/tiles.png")));

        calculateTileSize();
        // Если расстановка случайная, можно сразу создать старт, финиш, заполнить поле
        if (!manualPlacement) {
            generateRandomStartEnd();
            generateRandomField();
            startPlaced = true;
            endPlaced   = true;
        }

        createStatsPanel();

        VBox container = new VBox(10, grid, statsBox);
        container.setAlignment(Pos.TOP_CENTER);

        updateGrid();
        updateStatsPanel();

        root.getChildren().addAll(backgroundView, container, grid);

        // Запуск обновления статистики раз в 1 секунду
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateStatsPanel()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        // При закрытии окна — сохранение
        Scene scene = new Scene(root, WINDOW_SIZE, WINDOW_SIZE);
        windowManager.stage.setOnCloseRequest(e -> {
            e.consume();
            saveGame();
            Platform.exit();
        });

        return scene;
    }

    /**
     * Создаёт игровую сцену из загруженного состояния (loaded).
     */
    public Scene createGameSceneFromState(GameState loaded) {
        Image background = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/BG1.png")));
        ImageView backgroundView = new ImageView(background);
        backgroundView.setFitWidth(WINDOW_SIZE);
        backgroundView.setFitHeight(WINDOW_SIZE);

        grid = new GridPane();
        grid.setPadding(new Insets(350, 100, 300, 350));
        grid.setHgap(CELL_SPACING);
        grid.setVgap(CELL_SPACING);

        tiles = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/tiles.png")));

        calculateTileSize();

        createStatsPanel();
        VBox container = new VBox(10, grid, statsBox);
        container.setAlignment(Pos.TOP_CENTER);

        updateGrid();
        updateStatsPanel();

        root.getChildren().addAll(backgroundView, container, grid);

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateStatsPanel()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        Scene scene = new Scene(root, WINDOW_SIZE, WINDOW_SIZE);
        windowManager.stage.setOnCloseRequest(e -> {
            e.consume();
            saveGame();
            Platform.exit();
        });
        return scene;
    }

    // ------------------ ЛОГИКА СОХРАНЕНИЯ ------------------

    private void saveGame() {
        GameState state = new GameState();
        state.fieldSize = fieldSize;
        state.field     = field;
        state.visited   = visited;
        state.playerX   = playerX;
        state.playerY   = playerY;
        state.endX      = endX;
        state.endY      = endY;
        state.moves     = moves;
        state.visitedValues.addAll(visitedValues);
        windowManager.saveGameState(state);
    }

    // ------------------ ОБЩИЕ МЕТОДЫ ------------------

    private boolean isValidCoord(int x, int y) {
        return x >= 0 && x < fieldSize && y >= 0 && y < fieldSize;
    }

    /**
     * Создание панели статистики (шаги, сумма, счёт, время).
     */
    private void createStatsPanel() {
        statsBox = new VBox(20);
        statsBox.setAlignment(Pos.CENTER);

        Label movesLabel = new Label("Steps:");
        Label sumLabel   = new Label("Sum:");
        Label scoreLabel = new Label("Score:");
        Label timeLabel  = new Label("Time:");

        movesView = new ImageView();
        sumView   = new ImageView();
        scoreView = new ImageView();
        timerView = new ImageView();

        VBox movesBox = new VBox(5, movesLabel, movesView);
        VBox sumBox   = new VBox(5, sumLabel,   sumView);
        VBox scoreBox = new VBox(5, scoreLabel, scoreView);
        VBox timeBox  = new VBox(5, timeLabel,  timerView);

        // Ряд настроек для разных размеров поля
        if (fieldSize < 7) {
            statsBox.setScaleX(0.5);
            statsBox.setScaleY(0.5);
            statsBox.setPadding(new Insets(220, 0, 0, 0));
        } else if (fieldSize == 7) {
            statsBox.setPadding(new Insets(220, 0, 0, 260));
        } else if (fieldSize == 8) {
            statsBox.setScaleX(1.1);
            statsBox.setScaleY(1.1);
            statsBox.setPadding(new Insets(220, 0, 0, 280));
        } else if (fieldSize == 9) {
            statsBox.setScaleX(1.2);
            statsBox.setScaleY(1.2);
            statsBox.setPadding(new Insets(220, 0, 0, 290));
        } else if (fieldSize == 10) {
            statsBox.setScaleX(1.3);
            statsBox.setScaleY(1.3);
            statsBox.setPadding(new Insets(220, 0, 0, 310));
        }

        statsBox.getChildren().addAll(movesBox, sumBox, scoreBox, timeBox);

        movesLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16;");
        sumLabel.setStyle("-fx-text-fill: black; -fx-font-size: 16;");
        scoreLabel.setStyle("-fx-text-fill: black; -fx-font-size: 16;");
        timeLabel.setStyle("-fx-text-fill: black; -fx-font-size: 16;");
    }

    /**
     * Обновляет панель статистики.
     */
    private void updateStatsPanel() {
        int sum = visitedValues.stream().mapToInt(Integer::intValue).sum();
        int steps = moves;

        double sc = (steps == 0) ? 0 : (double) sum / steps;
        int elapsedSec = (int) ((System.currentTimeMillis() - startTime) / 1000);

        movesView.setImage(createScoreImage(steps));
        sumView.setImage(createScoreImage(sum));
        scoreView.setImage(createScoreImage((int) Math.round(sc)));
        timerView.setImage(createScoreImage(elapsedSec));
    }

    /**
     * Формируем картинку из цифровых тайлов для заданного числа.
     */
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

    private void generateRandomStartEnd() {
        Random random = new Random();
        playerX = random.nextInt(fieldSize);
        playerY = random.nextInt(fieldSize);
        field[playerX][playerY] = -1; // Start

        // Запомним стартовые координаты
        this.startX = playerX;
        this.startY = playerY;

        do {
            endX = random.nextInt(fieldSize);
            endY = random.nextInt(fieldSize);
        } while (isValidEndPosition(playerX, playerY, endX, endY));

        field[endX][endY] = -2; // End
    }

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

    private boolean isValidEndPosition(int sx, int sy, int ex, int ey) {
        if (sx == ex && sy == ey) return true;
        int dx = Math.abs(sx - ex);
        int dy = Math.abs(sy - ey);
        return (dx < 2 && dy < 2 && (dx != 1 || dy != 1));
    }

    private void calculateTileSize() {
        tileSize = Math.min((WINDOW_SIZE - 600) / fieldSize, ORIGINAL_TILE_SIZE);
    }

    private StackPane createCell(int x, int y) {
        StackPane cell = new StackPane();
        ImageView imageView = new ImageView(tiles);
        imageView.setViewport(getFieldViewport(field[x][y]));
        imageView.setFitWidth(tileSize);
        imageView.setFitHeight(tileSize);

        cell.getChildren().add(imageView);

        // Если мы в режиме показа лучшего пути — отключаем клики
        if (!bestPathMode) {
            cell.setOnMouseClicked(event -> handleCellClick(event, x, y));
        }
        return cell;
    }

    /**
     * Возвращает нужный фрагмент тайлсета для клетки поля (старта, финиша, цифры и т.д.).
     */
    private Rectangle2D getFieldViewport(int value) {
        // value: 1..9 => индексы 0..8
        // -1 => 9 (start), -2 => 11 (end), 0 => 10 (пустая)
        int index = switch (value) {
            case -1 -> 9;   // Start
            case -2 -> 11;  // End
            case 0  -> 10;  // Пустая
            default -> value - 1;
        };
        double x = index * ORIGINAL_TILE_SIZE;
        return new Rectangle2D(x, 0, ORIGINAL_TILE_SIZE, ORIGINAL_TILE_SIZE);
    }

    /**
     * Возвращает нужный фрагмент тайлсета для отображения цифры (0..9).
     */
    private Rectangle2D getDigitViewport(int digit) {
        int index = (digit == 0) ? 9 : (digit - 1);
        double x = index * ORIGINAL_TILE_SIZE;
        return new Rectangle2D(x, 0, ORIGINAL_TILE_SIZE, ORIGINAL_TILE_SIZE);
    }

    /**
     * Обработка клика по клетке (передвижение игрока, если можно).
     */
    private void handleCellClick(MouseEvent event, int x, int y) {
        if (bestPathMode) return; // если режим лучшего пути — не даём ходить

        // Ручная расстановка
        if (manualPlacement && !startPlaced) {
            field[x][y] = -1;
            playerX = x;
            playerY = y;
            startX = x;  // Запомним реальную точку старта
            startY = y;

            startPlaced = true;
            updateGrid();
            updateStatsPanel();
            return;
        }
        if (manualPlacement && !endPlaced) {
            if (isValidEndPosition(playerX, playerY, x, y)) return;
            field[x][y] = -2;
            endX = x;
            endY = y;
            endPlaced = true;

            generateRandomField();
            updateGrid();
            updateStatsPanel();
            return;
        }

        // Основная логика ходьбы (если старт и финиш уже есть)
        if (startPlaced && endPlaced) {
            if (isNeighbor(x, y) && !visited[x][y]) {
                visited[playerX][playerY] = true;
                visitedValues.add(field[x][y]);
                moves++;

                if (field[x][y] == -2) {
                    // пришли на финиш
                    updateStatsPanel();
                    endGame();
                    return;
                }

                // Сдвигаем "игрока" на новую позицию
                field[playerX][playerY] = 0; // то место, где был игрок, обнуляем
                playerX = x;
                playerY = y;
                field[playerX][playerY] = -1; // ставим -1 в новой позиции

                updateGrid();
                updateStatsPanel();
            }
        }
    }

    private boolean isNeighbor(int x, int y) {
        return (Math.abs(playerX - x) == 1 && playerY == y)
                || (Math.abs(playerY - y) == 1 && playerX == x);
    }

    /**
     * Перерисовка грида (grid) в соответствии с текущим состоянием field[][].
     */
    private void updateGrid() {
        grid.getChildren().clear();
        for (int i = 0; i < fieldSize; i++) {
            for (int j = 0; j < fieldSize; j++) {
                StackPane cell = createCell(i, j);
                grid.add(cell, j, i);
            }
        }
    }

    /**
     * Завершение игры: останавливаем таймер, считаем счёт игрока,
     * вызываем поиск лучшего пути (recursion) и переходим на экран итога.
     */
    private void endGame() {
        if (timeline != null) timeline.stop();

        int sum = visitedValues.stream().mapToInt(Integer::intValue).sum();
        double finalScore = (moves == 0) ? 0 : (double) sum / moves;

        // Считаем лучший счёт и лучшую последовательность (bestPath).
        // При этом bestScore обнуляем, чтобы пересчитать заново.
        this.bestScore = Double.MAX_VALUE;
        double bestScoreFound = computeBestScoreAndPath();

        long endTime = System.currentTimeMillis();
        int totalTimeSec = (int)((endTime - startTime) / 1000);

        visitedValues.clear();

        this.playerX = startX;
        this.playerY = startY;

        showEndScreen(finalScore, bestScoreFound, totalTimeSec);
    }

    // ------------------ РЕКУРСИВНЫЙ ПОИСК ЛУЧШЕГО ПУТИ ------------------

    /**
     * Рекурсивно ищем все пути. С учётом оптимизаций.
     *
     * @param costSoFar     набранная сумма очков
     * @param stepsSoFar    количество сделанных шагов (стартовые считаем за 1)
     * @param path          текущий маршрут (список координат)
     * @param visitedLocal  локальный массив посещённых ячеек
     * @param bestPathFound "выходной" список для хранения лучшего пути
     */
    private void backtrack(
            int x,
            int y,
            int costSoFar,
            int stepsSoFar,
            List<int[]> path,
            boolean[][] visitedLocal,
            List<int[]> bestPathFound
    ) {
        // 1. Проверка границ
        if (x < 0 || x >= fieldSize || y < 0 || y >= fieldSize) {
            return;
        }
        // 2. Если уже посещали — выходим
        if (visitedLocal[x][y]) {
            return;
        }

        visitedLocal[x][y] = true;
        path.add(new int[]{x, y});

        // 3. Если дошли до финиша — обновляем лучший результат
        if (x == endX && y == endY) {
            double score = (stepsSoFar == 0)
                    ? 0.0
                    : (double) costSoFar / (double) stepsSoFar;

            // Если нашли улучшение — запишем
            if (score < this.bestScore && score != 0) {
                this.bestScore = score;
                bestPathFound.clear();
                bestPathFound.addAll(path);
                System.out.println(score);
            }

            // Откат
            visitedLocal[x][y] = false;
            path.remove(path.size() - 1);
            return;
        }

        // 4. «Простая» оценка, можно ли улучшить ratio.
        //    Допустим, максимум клеток = fieldSize * fieldSize.
        //    Тогда оставшиеся клетки (или шаги) максимум = (fieldSize * fieldSize) - stepsSoFar.
        //    Даже если все они будут "бесплатными" (cost=0), то полученный ratio будет:
        //         costSoFar / (stepsSoFar + оставшиеся)
        //    Если это всё равно >= bestScore, то нет смысла углубляться дальше.
        int maxStepsLeft = fieldSize * fieldSize - stepsSoFar;
        double bestPossibleRatio = (stepsSoFar + maxStepsLeft > 0)
                ? ((double) costSoFar / (stepsSoFar + maxStepsLeft))
                : Double.MAX_VALUE;

        if (bestPossibleRatio >= this.bestScore) {
            // Прекращаем — в лучшем случае не станем лучше.
            visitedLocal[x][y] = false;
            path.remove(path.size() - 1);
            return;
        }

        // 5. Значение текущей клетки (для подсчёта costSoFar)
        int currentVal = field[x][y];
        if (currentVal < 0) currentVal = 0;  // старт/финиш не дают очков

        // 6. Четыре направления
        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};

        for (int i = 0; i < 4; i++) {
            int nx = x + dx[i];
            int ny = y + dy[i];

            // Посчитаем cost для следующего шага
            int nextCost = costSoFar;
            if (isValidCoord(nx, ny)) {
                int val = (field[nx][ny] >= 1) ? field[nx][ny] : 0;
                nextCost += val;
            }

            // Рекурсивный вызов
            backtrack(nx, ny, nextCost, stepsSoFar + 1, path, visitedLocal, bestPathFound);
        }

        // 7. Откат
        visitedLocal[x][y] = false;
        path.remove(path.size() - 1);
    }

    /**
     * Запуск рекурсивного перебора. Возвращает лучший счёт (минимальный) или 9999.0,
     * если пути нет. Также заполняет bestPath (список координат оптимального пути).
     */
    private double computeBestScoreAndPath() {
        System.out.println("Start computeBestScoreAndPath");
        bestPath = new ArrayList<>();

        // Лучшее значение храним в this.bestScore — туда же будем записывать результат
        // при нахождении лучших путей.
        this.bestScore = Double.MAX_VALUE;
        List<int[]> bestPathFound = new ArrayList<>();

        boolean[][] visitedLocal = new boolean[fieldSize][fieldSize];

        // Стартуем из (startX, startY)
        backtrack(
                startX,
                startY,
                0,
                1,
                new ArrayList<>(),
                visitedLocal,
                bestPathFound
        );

        // Если так и не нашли путь
        if (this.bestScore == Double.MAX_VALUE) {
            bestPath.clear();
            return 9999.0;
        }

        System.out.println("Best score is: " + bestScore);
        bestPath = bestPathFound;
        return this.bestScore;
    }

    // ----------------------------------------------------------------------

    /**
     * Показываем экран победы/поражения и счёты.
     */
    private void showEndScreen(double finalScore, double bestScore, int totalTimeSec) {
        Image background = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/BG1.png")));
        ImageView backgroundView = new ImageView(background);
        backgroundView.setFitWidth(WINDOW_SIZE);
        backgroundView.setFitHeight(WINDOW_SIZE);
        //Math.round(finalScore);
        //Math.round(bestScore);

        boolean isWin = (Double.compare(finalScore, bestScore) <= 0);
        System.out.println("Player score is: " + finalScore + ". Best score is: " + bestScore);

        ImageView resultView = new ImageView(new Image(
                Objects.requireNonNull(
                        getClass().getResourceAsStream(isWin ? "/WIN.png" : "/LOST.png")
                )));
        int temp = 2;
        resultView.setFitWidth(370/temp);
        resultView.setFitHeight(726/temp);
        resultView.setTranslateY(0);
        resultView.setTranslateX(-150);

        ImageView playerScoreView = createScoreView((int) Math.round(finalScore));
        playerScoreView.setTranslateY(0);

        ImageView bestScoreView = createScoreView((int) Math.round(bestScore));
        bestScoreView.setTranslateY(80);

        ImageView timeView = createScoreView(totalTimeSec);
        timeView.setTranslateY(180);

        Button toMenuButton = createImageButton("/toMenu.png");
        toMenuButton.setOnAction(event -> goToMenu());

        Button betterWayButton = createImageButton("/bestWay.png");
        // При нажатии показываем лучший путь
        betterWayButton.setOnAction(event -> {
            // Сохраняем текущие ноды экрана
            endScreenNodesBackup.clear();
            endScreenNodesBackup.addAll(root.getChildren());

            // Чтобы нельзя было ходить по полю
            bestPathMode = true;

            // Очищаем root, показываем только фон, grid и кнопку «Return»
            root.getChildren().clear();
            root.getChildren().addAll(backgroundView, grid);

            // Добавим кнопку «Return»
            Button returnButton = new Button("Return");
            returnButton.setTranslateX(380);
            returnButton.setTranslateY(300);
            returnButton.setStyle("-fx-font-size: 16;");
            returnButton.setOnAction(e2 -> {
                // Останавливаем мигание
                if (blinkTimeline != null) {
                    blinkTimeline.stop();
                }
                // Возвращаемся к экрану победы/проигрыша
                bestPathMode = false;
                root.getChildren().clear();
                root.getChildren().addAll(endScreenNodesBackup);
            });
            root.getChildren().add(returnButton);

            // Запускаем мигание лучшего пути
            highlightBestPathBlink();
        });

        VBox buttons = new VBox(20, toMenuButton, betterWayButton);
        buttons.setTranslateY(65);
        buttons.setTranslateX(320);
        buttons.setScaleX(0.5);
        buttons.setScaleY(0.5);

        // Очищаем root и собираем заново
        root.getChildren().clear();
        root.getChildren().addAll(backgroundView, resultView, playerScoreView, bestScoreView, timeView, buttons);
    }

    /**
     * Создаёт кнопку на основе картинки.
     */
    private Button createImageButton(String texturePath) {
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(texturePath)));
        ImageView imageView = new ImageView(image);
        Button button = new Button();
        button.setGraphic(imageView);
        button.setStyle("-fx-background-color: transparent;");
        return button;
    }

    /**
     * Формирует ImageView с «цифровым» отображением числа scoreValue (для счёта/времени).
     */
    private ImageView createScoreView(int scoreValue) {
        HBox scoreTiles = new HBox(5);
        scoreTiles.setStyle("-fx-background-color: transparent;");

        String str = String.valueOf(scoreValue);
        for (char c : str.toCharArray()) {
            int digit = Character.getNumericValue(c);
            if (digit < 0 || digit > 9) continue;

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

    /**
     * Переход в главное меню.
     */
    private void goToMenu() {
        windowManager.initialize(windowManager.stage);
    }

    /**
     * Мигание пути:
     * - Старт (startX,startY) отображаем как -1 (иконка игрока),
     * - Финиш (endX,endY) как -2 (иконка финиша),
     * - Промежуточные клетки пути мигают «цифра / обычная плитка».
     */
    private void highlightBestPathBlink() {
        // Обновим поле (grid) — в нём уже отключены клики
        updateGrid();

        blinkTimeline = new Timeline(
                new KeyFrame(Duration.seconds(0.0), e -> {
                    // Показываем цифры на всех клетках пути, кроме старт/финиш
                    for (int[] coords : bestPath) {
                        int bx = coords[0];
                        int by = coords[1];
                        StackPane sp = getCellFromGrid(bx, by);

                        if (bx == startX && by == startY) {
                            // старт
                            sp.setStyle("-fx-background-color: transparent;");
                            setCellViewport(sp, -1);
                        } else if (bx == endX && by == endY) {
                            // финиш
                            sp.setStyle("-fx-background-color: transparent;");
                            setCellViewport(sp, -2);
                        } else {
                            //TODO: fix it

                            // промежуточная клетка — показываем "цифру"
                            sp.setStyle("-fx-background-color: transparent;");
                            int val = field[bx][by];
                            // Если в исходном поле у нас 0 (или -1/-2),
                            // можно показать хотя бы 1, чтобы что-то мигало
                            setCellViewport(sp, (val > 0) ? val : 1);
                        }
                    }
                }),
                new KeyFrame(Duration.seconds(1.0), e -> {
                    // Показываем «обычную плитку» для промежуточных
                    for (int[] coords : bestPath) {
                        int bx = coords[0];
                        int by = coords[1];
                        StackPane sp = getCellFromGrid(bx, by);

                        if (bx == startX && by == startY) {
                            setCellViewport(sp, -1);
                        } else if (bx == endX && by == endY) {
                            setCellViewport(sp, -2);
                        } else {
                            sp.setStyle("-fx-background-color: transparent;");
                            // Показываем «текстуру» клетки
                            int v = field[bx][by];
                            if (v < 0) v = 0;
                            setCellViewport(sp, v);
                        }
                    }
                })
        );

        blinkTimeline.setCycleCount(Timeline.INDEFINITE);
        blinkTimeline.setAutoReverse(true);
        blinkTimeline.play();
    }

    /**
     * Устанавливает в StackPane (ячейку) спрайт из tiles для заданного value.
     */
    private void setCellViewport(StackPane cell, int value) {
        if (cell.getChildren().isEmpty()) return;
        ImageView iv = (ImageView) cell.getChildren().get(0);
        iv.setViewport(getFieldViewport(value));
    }

    private StackPane getCellFromGrid(int x, int y) {
        for (Node node : grid.getChildren()) {
            if (GridPane.getRowIndex(node) == x && GridPane.getColumnIndex(node) == y) {
                return (StackPane) node;
            }
        }
        return null;
    }
}
