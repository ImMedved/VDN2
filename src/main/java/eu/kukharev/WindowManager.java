package eu.kukharev;

import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * Главный менеджер окон (переключение сцен, сохранение/загрузка).
 */
public class WindowManager {
    public Stage stage;

    /**
     * Инициализация главного окна (стартовое меню).
     */
    public void initialize(Stage primaryStage) {
        this.stage = primaryStage;
        MenuManager menuManager = new MenuManager(this);

        Scene menuScene = menuManager.createMenu();
        stage.setTitle("Game Application");
        stage.setScene(menuScene);
        stage.setWidth(1000);
        stage.setHeight(1000);
        stage.setResizable(false);
        stage.show();
    }

    /**
     * Сохраняет состояние игры в файл savegame.txt.
     */
    public void saveGameState(GameState state) {
        try (PrintWriter writer = new PrintWriter("savegame.txt")) {
            writer.println(state.fieldSize);
            writer.println(state.playerX + ";" + state.playerY);
            writer.println(state.endX + ";" + state.endY);
            writer.println(state.moves);

            // visitedValues (пройденные числа)
            StringBuilder visitedVals = new StringBuilder();
            for (Integer val : state.visitedValues) {
                visitedVals.append(val).append(",");
            }
            writer.println(visitedVals);

            // Поле
            for (int i = 0; i < state.fieldSize; i++) {
                for (int j = 0; j < state.fieldSize; j++) {
                    writer.print(state.field[i][j]);
                    if (j < state.fieldSize - 1) writer.print(",");
                }
                writer.println();
            }

            // visited (маска посещённых)
            for (int i = 0; i < state.fieldSize; i++) {
                for (int j = 0; j < state.fieldSize; j++) {
                    writer.print(state.visited[i][j] ? 1 : 0);
                    if (j < state.fieldSize - 1) writer.print(",");
                }
                writer.println();
            }

            System.out.println("Game saved to savegame.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Загружает состояние игры из файла savegame.txt.
     *
     * @return GameState или null, если файл отсутствует или произошла ошибка
     */
    public GameState loadGameState() {
        File file = new File("savegame.txt");
        if (!file.exists()) {
            System.out.println("Save file not found.");
            return null;
        }
        try (Scanner sc = new Scanner(file)) {
            GameState state = new GameState();
            state.fieldSize = Integer.parseInt(sc.nextLine());

            String[] playerPos = sc.nextLine().split(";");
            state.playerX = Integer.parseInt(playerPos[0]);
            state.playerY = Integer.parseInt(playerPos[1]);

            String[] endPos = sc.nextLine().split(";");
            state.endX = Integer.parseInt(endPos[0]);
            state.endY = Integer.parseInt(endPos[1]);

            state.moves = Integer.parseInt(sc.nextLine());

            String visitedValsLine = sc.nextLine();
            if (!visitedValsLine.isEmpty()) {
                String[] splitted = visitedValsLine.split(",");
                for (String s : splitted) {
                    if (!s.isEmpty()) {
                        state.visitedValues.add(Integer.parseInt(s));
                    }
                }
            }

            state.field = new int[state.fieldSize][state.fieldSize];
            for (int i = 0; i < state.fieldSize; i++) {
                String line = sc.nextLine();
                String[] splitted = line.split(",");
                for (int j = 0; j < state.fieldSize; j++) {
                    state.field[i][j] = Integer.parseInt(splitted[j]);
                }
            }

            state.visited = new boolean[state.fieldSize][state.fieldSize];
            for (int i = 0; i < state.fieldSize; i++) {
                String line = sc.nextLine();
                String[] splitted = line.split(",");
                for (int j = 0; j < state.fieldSize; j++) {
                    state.visited[i][j] = splitted[j].equals("1");
                }
            }

            System.out.println("Game loaded from savegame.txt");
            return state;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Устанавливает сцену в текущее окно приложения.
     */
    public void setScene(Scene scene) {
        stage.setScene(scene);
    }
}
