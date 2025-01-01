package eu.kukharev;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Cell {
    private final int value;

    public Cell(int value) {
        this.value = value;
    }

    public Node getNode() {
        String texture = switch (value) {
            case -1 -> "/Start.png";
            case -2 -> "/End.png";
            default -> "/tiles.png"; // По умолчанию тайл
        };

        Image image = new Image(getClass().getResourceAsStream(texture));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(20);
        imageView.setFitHeight(20);

        return imageView;
    }
}
