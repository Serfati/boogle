package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Model;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import view_model.ViewModel;

/* class for the program main run */
public class Main extends Application {
    private final static Logger LOGGER = LogManager.getLogger(Main.class.getName());

    /**
     * The main function that runs the entire program
     * @param args - ignored
     */
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        LOGGER.log(Level.INFO, "Boogle launched");
        launch(args);
        long exitTime = System.currentTimeMillis();
        LOGGER.log(Level.INFO, "Boogle is closing. Used for "+(exitTime-startTime) / 60000.0+" m");
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Model model = new Model();
        ViewModel viewModel = new ViewModel(model);
        model.addObserver(viewModel);
        //------------------------------//
        primaryStage.setTitle("BOOGLE");
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ui.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 620, 700);
        scene.getStylesheets().add(getClass().getResource("/dark-style.css").toExternalForm());
        primaryStage.setResizable(false);
        //------------ui---------------//
        UIController viewController = fxmlLoader.getController();
        viewController.setViewModel(viewModel);
        viewModel.addObserver(viewController);
        //------------------------------//
        primaryStage.setScene(scene);
        SetStageCloseEvent(primaryStage, viewController);
        primaryStage.show();
        //------------------------------//
    }

    private void SetStageCloseEvent(Stage primaryStage, UIController viewController) {
        primaryStage.setOnCloseRequest(windowEvent -> {
            viewController.exitCorrectly();
            windowEvent.consume();
        });
    }
}