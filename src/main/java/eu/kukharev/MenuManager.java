package eu.kukharev;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import java.util.Objects;

/**
 * Менеджер главного меню игры.
 */
public class MenuManager {
    private final WindowManager windowManager;

    /**
     * @param windowManager Менеджер окон
     */
    public MenuManager(WindowManager windowManager) {
        this.windowManager = windowManager;
    }

    /**
     * Создаёт сцену главного меню с кнопками «New Game», «Rules», «Load», «Exit».
     */
    public Scene createMenu() {
        StackPane root = new StackPane();

        // Фон
        Image background = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/BG1.png")));
        ImageView backgroundView = new ImageView(background);
        backgroundView.setFitWidth(1000);
        backgroundView.setFitHeight(1000);

        // Создание кнопок
        Button newGameButton = createImageButton("/SG.png");
        Button rulesButton   = createImageButton("/RULES.png");
        Button exitButton    = createImageButton("/EXIT.png");
        Button loadButton    = createImageButton("/LOAD.png");

        // Масштабирование кнопок
        newGameButton.setScaleX(0.5);
        newGameButton.setScaleY(0.5);
        rulesButton.setScaleX(0.5);
        rulesButton.setScaleY(0.5);
        exitButton.setScaleX(0.25);
        exitButton.setScaleY(0.25);
        loadButton.setScaleX(0.25);
        loadButton.setScaleY(0.25);

        // Обработчики
        newGameButton.setOnAction(event -> goToNewGameSetup());
        rulesButton.setOnAction(event -> showRules(root));
        exitButton.setOnAction(event -> Platform.exit());
        loadButton.setOnAction(event -> loadSavedGame());

        // Сетка для кнопок
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(-500);
        gridPane.setVgap(-350);

        // Раскладываем кнопки в сетке
        gridPane.add(newGameButton, 0, 0);
        gridPane.add(rulesButton,   1, 0);
        gridPane.add(loadButton,    0, 1);
        gridPane.add(exitButton,    1, 1);

        // Настраиваем отступы
        GridPane.setMargin(newGameButton, new Insets(200, 0, 0, 200));
        GridPane.setMargin(rulesButton,   new Insets(200, 0, 0, 170));
        GridPane.setMargin(loadButton,    new Insets(50, 0, 0, 0));
        GridPane.setMargin(exitButton,    new Insets(50, 0, 0, 0));

        root.getChildren().addAll(backgroundView, gridPane);

        return new Scene(root, 1000, 1000);
    }

    private void loadSavedGame() {
        GameState loaded = windowManager.loadGameState();
        if (loaded != null) {
            // Создаём GameFieldManager с готовым состоянием
            GameFieldManager manager = new GameFieldManager(windowManager, loaded);
            windowManager.setScene(manager.createGameSceneFromState(loaded));
        }
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
     * Показываем правила игры (смена фона).
     */
    private void showRules(StackPane root) {
        Image background = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/BG2.png")));
        ImageView backgroundView = new ImageView(background);
        backgroundView.setFitWidth(1000);
        backgroundView.setFitHeight(1000);

        root.getChildren().clear();
        root.getChildren().add(backgroundView);
        // Возврат в меню по клику
        root.setOnMouseClicked(event -> windowManager.initialize(windowManager.stage));
    }

    /**
     * Переход к сцене настроек новой игры.
     */
    private void goToNewGameSetup() {
        NewGameSetupManager setupManager = new NewGameSetupManager(windowManager);
        windowManager.setScene(setupManager.createSetupScene());
    }
}
