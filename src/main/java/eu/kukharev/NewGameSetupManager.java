package eu.kukharev;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

import java.util.Objects;

public class NewGameSetupManager {
    private final WindowManager windowManager;
    private int selectedFieldSize = 5;

    // Ручная расстановка или случайная
    private boolean manualPlacement = true;

    public NewGameSetupManager(WindowManager windowManager) {
        this.windowManager = windowManager;
    }

    public Scene createSetupScene() {
        StackPane root = new StackPane();

        // Задний фон
        Image background = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/BG1.png")));
        ImageView backgroundView = new ImageView(background);
        backgroundView.setFitWidth(1000);
        backgroundView.setFitHeight(1000);

        VBox menuBox = new VBox(15);
        menuBox.setPadding(new Insets(50));
        menuBox.setTranslateY(100);

        Text label = new Text("Настройки новой игры:");
        label.setStyle("-fx-font-size: 24; -fx-fill: white;");

        // Выбор размера поля
        HBox fieldSizeBox = new HBox(10);
        Text sizeLabel = new Text("Размер поля:");
        sizeLabel.setStyle("-fx-font-size: 18; -fx-fill: white;");
        ComboBox<Integer> sizeSelector = new ComboBox<>();
        sizeSelector.getItems().addAll(3, 4, 5, 6, 7, 8, 9, 10);
        sizeSelector.setValue(selectedFieldSize);
        sizeSelector.setOnAction(event -> selectedFieldSize = sizeSelector.getValue());
        fieldSizeBox.getChildren().addAll(sizeLabel, sizeSelector);

        // Радиокнопки "Ручная расстановка / Случайная"
        ToggleGroup placementGroup = new ToggleGroup();
        RadioButton manualButton = new RadioButton("Ручная расстановка");
        manualButton.setToggleGroup(placementGroup);
        manualButton.setSelected(true);
        RadioButton randomButton = new RadioButton("Случайная расстановка");
        randomButton.setToggleGroup(placementGroup);

        manualButton.setOnAction(e -> manualPlacement = true);
        randomButton.setOnAction(e -> manualPlacement = false);

        HBox placementBox = new HBox(20, manualButton, randomButton);
        placementBox.setTranslateY(10);

        // Кнопка старта
        Button startButton = new Button("Start Game");
        startButton.setStyle("-fx-font-size: 16;");
        startButton.setOnAction(e -> startGame());

        menuBox.getChildren().addAll(label, fieldSizeBox, placementBox, startButton);

        root.getChildren().addAll(backgroundView, menuBox);
        return new Scene(root, 1000, 1000);
    }

    private void startGame() {
        // Передаем выбранные настройки в GameFieldManager
        GameFieldManager gameFieldManager = new GameFieldManager(windowManager, selectedFieldSize, manualPlacement);
        windowManager.setScene(gameFieldManager.createGameScene());
    }
}