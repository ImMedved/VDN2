package eu.kukharev;

import javafx.scene.Scene;
import javafx.stage.Stage;

public class WindowManager {
    public Stage stage;
    private Scene currentGameScene;

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

    public void saveGameScene(Scene scene) {
        this.currentGameScene = scene;
    }

    public Scene getCurrentGameScene() {
        return currentGameScene;
    }

    public void setScene(Scene scene) {
        stage.setScene(scene);
    }
}
