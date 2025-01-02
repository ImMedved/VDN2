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

        // Фон
        Image background = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/BG1.png")));
        ImageView backgroundView = new ImageView(background);
        backgroundView.setFitWidth(1000);
        backgroundView.setFitHeight(1000);

        VBox menuOptions = new VBox(20);
        menuOptions.setTranslateY(250);
        menuOptions.setTranslateX(300);

        Button newGameButton = createImageButton("/SG.png");  // "New Game"
        Button rulesButton  = createImageButton("/RULES.png");

        newGameButton.setScaleX(0.6);
        newGameButton.setScaleY(0.6);
        rulesButton.setScaleX(0.6);
        rulesButton.setScaleY(0.6);

        // Переходим на экран настроек новой игры
        newGameButton.setOnAction(event -> goToNewGameSetup());
        rulesButton.setOnAction(event -> showRules(root));

        menuOptions.getChildren().addAll(newGameButton, rulesButton);
        root.getChildren().addAll(backgroundView, menuOptions);

        return new Scene(root, 1000, 1000);
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
        Image background = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/BG2.png")));
        ImageView backgroundView = new ImageView(background);
        backgroundView.setFitWidth(1000);
        backgroundView.setFitHeight(1000);

        root.getChildren().clear();
        root.getChildren().add(backgroundView);
        root.setOnMouseClicked(event -> windowManager.initialize(windowManager.stage));
    }

    private void goToNewGameSetup() {
        NewGameSetupManager setupManager = new NewGameSetupManager(windowManager);
        Scene setupScene = setupManager.createSetupScene();
        windowManager.setScene(setupScene);
    }
}
