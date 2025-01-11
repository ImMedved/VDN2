package eu.kukharev;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Точка входа в приложение.
 */
public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        WindowManager windowManager = new WindowManager();
        windowManager.initialize(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
