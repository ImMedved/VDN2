package eu.kukharev;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Objects;

public class MenuManager {
    private final WindowManager windowManager;

    public MenuManager(WindowManager windowManager) {
        this.windowManager = windowManager;
    }

    public Scene createMenu() {
        StackPane root = new StackPane();

        // Задний фон
        Image background = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/BG1.png")));
        ImageView backgroundView = new ImageView(background);
        backgroundView.setFitWidth(1000);
        backgroundView.setFitHeight(1000);

        // Кнопки
        VBox buttons = new VBox(-50);
        buttons.setTranslateY(250);
        buttons.setTranslateX(300);

        Button startGameButton = createImageButton("/SG.png");
        Button rulesButton = createImageButton("/RULES.png");

        startGameButton.setScaleX(0.6);
        startGameButton.setScaleY(0.6);

        rulesButton.setScaleX(0.6);
        rulesButton.setScaleY(0.6);

        startGameButton.setOnAction(event -> startGame());
        rulesButton.setOnAction(event -> showRules(root));

        buttons.getChildren().addAll(startGameButton, rulesButton);
        root.getChildren().addAll(backgroundView, buttons);

        return new Scene(root, 1000, 1000);
    }

    private Button createImageButton(String texturePath) {
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(texturePath)));
        ImageView imageView = new ImageView(image);
        Button button = new Button();
        button.setGraphic(imageView);
        button.setStyle("-fx-background-color: transparent;"); // Прозрачный фон кнопки
        return button;
    }

    private void startGame() {
        GameFieldManager gameFieldManager = new GameFieldManager();
        windowManager.setScene(gameFieldManager.createGameScene());
    }

    private void showRules(StackPane root) {
        Image background = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/BG2.png")));
        ImageView backgroundView = new ImageView(background);
        backgroundView.setFitWidth(1000);
        backgroundView.setFitHeight(1000);

        root.getChildren().clear();
        root.getChildren().add(backgroundView);
        root.setOnMouseClicked(event -> windowManager.initialize(windowManager.stage));
    }
}
