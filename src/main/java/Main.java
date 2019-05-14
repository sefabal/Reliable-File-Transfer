import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent myLoader = FXMLLoader.load((getClass().getResource("fxml/MainMenu.fxml")));
        primaryStage.setResizable(false);
        primaryStage.getIcons().addAll(new Image("transferIcon.png"));
        primaryStage.setTitle("File Transfer Application");
        primaryStage.setScene(new Scene(myLoader));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
