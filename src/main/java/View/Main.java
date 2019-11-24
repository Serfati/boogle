package View;

import Model.MyModel;
import ViewModel.MyViewModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/* class for the program main run */
public class Main extends Application {

    /**
     * The main function that runs the entire program
     *
     * @param args - ignored
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        MyModel model = new MyModel();
        MyViewModel myViewModel = new MyViewModel(model);
        model.addObserver(myViewModel);
        //------------------------------//
        primaryStage.setTitle("BOOGLE");
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("MyView.fxml").openStream());
        Scene scene = new Scene(root, 800, 700);
        scene.getStylesheets().add(getClass().getResource("ViewStyle.css").toExternalForm());
        //------------------------------//
        MyViewController myViewController = fxmlLoader.getController();
        myViewController.setViewModel(myViewModel);
        myViewModel.addObserver(myViewController);
        //------------------------------//
        scene.addEventFilter(KeyEvent.KEY_PRESSED, myViewController::KeyPressed);
        primaryStage.setScene(scene);
        SetStageCloseEvent(primaryStage, myViewController);
        primaryStage.show();
        //------------------------------//
    }

    private void SetStageCloseEvent(Stage primaryStage, MyViewController myViewController) {
        primaryStage.setOnCloseRequest(windowEvent -> {
            myViewController.exitCorrectly();
            windowEvent.consume();
        });
    }
}