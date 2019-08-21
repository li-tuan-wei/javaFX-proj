package ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Created by ldh on 2019/3/19.
 */
public class ImageTest extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        ImageView imageView = new ImageView();
        Image image = new Image(ImageTest.class.getResource("/bg1.jpg").toExternalForm());
        imageView.setImage(image);
        ChangeImageView changeImageView = new ChangeImageView(imageView);
        imageView.setOnMouseClicked(e->{
            changeImageView.changeImageView();
        });
        StackPane stackPane = new StackPane();
        stackPane.getChildren().add(imageView);
        Scene scene = new Scene(stackPane, 200, 100);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static class ChangeImageView {

        private ImageView imageView ;

        public ChangeImageView(ImageView imageView) {
            this.imageView = imageView;
        }

        public void changeImageView() {
            Image image2 = new Image(ImageTest.class.getResource("/bg4.jpg").toExternalForm());
            imageView.setImage(image2);
        }
    }
}
