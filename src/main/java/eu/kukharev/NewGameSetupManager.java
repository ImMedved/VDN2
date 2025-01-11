package eu.kukharev;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import java.util.Objects;
import java.util.Random;

/**
 * Менеджер настроек новой игры.
 */
public class NewGameSetupManager {
    private final WindowManager windowManager;
    private int selectedFieldSize = 5;
    private boolean manualPlacement = true; // по умолчанию — ручная расстановка

    /**
     * @param windowManager Менеджер окон
     */
    public NewGameSetupManager(WindowManager windowManager) {
        this.windowManager = windowManager;
    }

    /**
     * Создаёт сцену настроек (выбор размера поля, ручная/случайная расстановка).
     */
    public Scene createSetupScene() {
        StackPane root = new StackPane();

        // Фон
        Image background = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/BG1.png")));
        ImageView backgroundView = new ImageView(background);
        backgroundView.setFitWidth(1000);
        backgroundView.setFitHeight(1000);

        VBox menuBox = new VBox(15);
        menuBox.setPadding(new Insets(500));
        menuBox.setTranslateY(100);

        Text label = new Text("New Game Settings:");
        label.setStyle("-fx-font-size: 24; -fx-fill: black;");

        // Выбор размера поля
        HBox fieldSizeBox = new HBox(10);
        Text sizeLabel = new Text("Field size:");
        sizeLabel.setStyle("-fx-font-size: 18; -fx-fill: black;");

        ComboBox<Integer> sizeSelector = new ComboBox<>();
        sizeSelector.getItems().addAll(3, 4, 5, 6, 7, 8, 9, 10);
        sizeSelector.setValue(selectedFieldSize);
        sizeSelector.setOnAction(event -> selectedFieldSize = sizeSelector.getValue());

        fieldSizeBox.getChildren().addAll(sizeLabel, sizeSelector);

        // Радиокнопки
        ToggleGroup placementGroup = new ToggleGroup();
        RadioButton manualButton = new RadioButton("Manual start/end");
        manualButton.setToggleGroup(placementGroup);
        manualButton.setSelected(true);

        RadioButton randomButton = new RadioButton("Random start/end");
        randomButton.setToggleGroup(placementGroup);

        manualButton.setOnAction(e -> manualPlacement = true);
        randomButton.setOnAction(e -> manualPlacement = false);

        Button startButton = new Button("Start Game");
        startButton.setStyle("-fx-font-size: 16;");
        startButton.setOnAction(e -> startGame());

        menuBox.getChildren().addAll(label, fieldSizeBox, manualButton, randomButton, startButton);

        root.getChildren().addAll(backgroundView, menuBox);
        return new Scene(root, 1000, 1000);
    }

    private void startGame() {
        // Если пользователь выбрал «случайную расстановку», выбираем случайный размер
        if (!manualPlacement) {
            Random rnd = new Random();
            selectedFieldSize = rnd.nextInt(8) + 3; // [3..10]
        }

        GameFieldManager gameFieldManager = new GameFieldManager(
                windowManager,
                selectedFieldSize,
                manualPlacement
        );
        windowManager.setScene(gameFieldManager.createGameScene());
    }
}
