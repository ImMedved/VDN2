package eu.kukharev;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.util.Objects;

public class MenuManager {
    private final WindowManager windowManager;

    public MenuManager(WindowManager windowManager) {
        this.windowManager = windowManager;
    }

    public Scene createMenu() {
        StackPane root = new StackPane();

        // Фон
        Image background = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/BG1.png")));
        ImageView backgroundView = new ImageView(background);
        backgroundView.setFitWidth(1000);
        backgroundView.setFitHeight(1000);

        // Создание кнопок
        Button newGameButton = createImageButton("/SG.png");
        Button rulesButton = createImageButton("/RULES.png");
        Button exitButton = createImageButton("/EXIT.png");
        Button loadButton = createImageButton("/LOAD.png");

        // Настройка масштабов кнопок
        newGameButton.setScaleX(0.5);
        newGameButton.setScaleY(0.5);
        rulesButton.setScaleX(0.5);
        rulesButton.setScaleY(0.5);
        exitButton.setScaleX(0.25);
        exitButton.setScaleY(0.25);
        loadButton.setScaleX(0.25);
        loadButton.setScaleY(0.25);

        // Добавление обработчиков событий
        newGameButton.setOnAction(event -> goToNewGameSetup());
        rulesButton.setOnAction(event -> showRules(root));
        exitButton.setOnAction(event -> Platform.exit());
        loadButton.setOnAction(event -> loadSavedGame());

        // Создание GridPane
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER); // Центрирование всей сетки
        gridPane.setHgap(-500); // Горизонтальный промежуток между кнопками
        gridPane.setVgap(-350); // Вертикальный промежуток между кнопками

        // Добавление кнопок в сетку 2x2
        gridPane.add(newGameButton, 0, 0); // Первая строка, первый столбец
        gridPane.add(rulesButton, 1, 0);   // Первая строка, второй столбец
        gridPane.add(loadButton, 0, 1);    // Вторая строка, первый столбец
        gridPane.add(exitButton, 1, 1);    // Вторая строка, второй столбец

        // Сдвиг верхнего ряда вправо
        GridPane.setMargin(newGameButton, new Insets(200, 0, 0, 200)); // Левый отступ для кнопки
        GridPane.setMargin(rulesButton, new Insets(200, 0, 0, 170));   // Левый отступ для кнопки
        GridPane.setMargin(loadButton, new Insets(50, 0, 0, 0)); // Левый отступ для кнопки
        GridPane.setMargin(exitButton, new Insets(50, 0, 0, 0));   // Левый отступ для кнопки

        // Добавление фона и сетки в корневой элемент
        root.getChildren().addAll(backgroundView, gridPane);

        return new Scene(root, 1000, 1000);
    }

    private void loadSavedGame() {
        GameState loaded = windowManager.loadGameState();
        if (loaded != null) {
            // Создадим GameFieldManager, который умеет принимать заранее готовый GameState
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

    private void showRules(StackPane root) {
        // Можно переиспользовать BG1 или BG2 по желанию
        Image background = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/BG2.png")));
        ImageView backgroundView = new ImageView(background);
        backgroundView.setFitWidth(1000);
        backgroundView.setFitHeight(1000);

        root.getChildren().clear();
        root.getChildren().add(backgroundView);
        // Возврат в меню по клику
        root.setOnMouseClicked(event -> windowManager.initialize(windowManager.stage));
    }

    private void goToNewGameSetup() {
        NewGameSetupManager setupManager = new NewGameSetupManager(windowManager);
        Scene setupScene = setupManager.createSetupScene();
        windowManager.setScene(setupScene);
    }
}
