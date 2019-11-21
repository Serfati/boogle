package View;

import Controller.Controller;
import Model.Model;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Model model = new Model();

        primaryStage.setTitle("BOOGLE");
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("MyView.fxml").openStream());
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);

        View view = fxmlLoader.getController();
        Controller controller = new Controller();

        model.setController(controller);
        view.setController(controller);

        primaryStage.show();
    }
}