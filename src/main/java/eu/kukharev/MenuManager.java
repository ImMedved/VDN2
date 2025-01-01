package eu.kukharev;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MenuManager {
    private final WindowManager windowManager;

    public MenuManager(WindowManager windowManager) {
        this.windowManager = windowManager;
    }

    public Scene createMenu() {
        StackPane root = new StackPane();

        Image background = new Image(getClass().getResourceAsStream("/BG1.png"));
        ImageView backgroundView = new ImageView(background);
        backgroundView.setFitWidth(804);
        backgroundView.setFitHeight(804);

        VBox buttons = new VBox(20);
        Button startGameButton = new Button("Start Game");
        Button rulesButton = new Button("Rules");

        startGameButton.setOnAction(event -> startGame());
        rulesButton.setOnAction(event -> showRules(root));

        buttons.getChildren().addAll(startGameButton, rulesButton);
        root.getChildren().addAll(backgroundView, buttons);
        return new Scene(root, 804, 804);
    }

    private void startGame() {
        GameFieldManager gameFieldManager = new GameFieldManager();
        windowManager.setScene(gameFieldManager.createGameScene());
    }

    private void showRules(StackPane root) {
        Image background = new Image(getClass().getResourceAsStream("/BG2.png"));
        ImageView backgroundView = new ImageView(background);
        backgroundView.setFitWidth(804);
        backgroundView.setFitHeight(804);

        root.getChildren().clear();
        root.getChildren().add(backgroundView);
        root.setOnMouseClicked(event -> windowManager.initialize(windowManager.stage));
    }
}
