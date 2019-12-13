import Model.MyModel;
import ViewModel.ViewModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;

/* class for the program main run */
public class Main extends Application {
    private final static Logger LOGGER = LogManager.getLogger(Main.class.getName());

    /**
     * The main function that runs the entire program
     *
     * @param args - ignored
     */
    public static void main(String[] args) {
        Long startTime = System.currentTimeMillis();
        LOGGER.log(Level.INFO, "Boogle launched on "+formatDateTimeString(startTime));
        launch(args);
        long exitTime = System.currentTimeMillis();
        LOGGER.log(Level.INFO, "Boogle is closing on "+formatDateTimeString(startTime)+". Used for "+(exitTime-startTime) / 60000.0+" m");
    }

    public static String formatDateTimeString(Long time) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        return formatter.format(new Date(time));
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        MyModel model = new MyModel();
        ViewModel viewModel = new ViewModel(model);
        model.addObserver(viewModel);
        //------------------------------//
        primaryStage.setTitle("BOOGLE");
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("UI.fxml").openStream());
        Scene scene = new Scene(root, 620, 700);
        scene.getStylesheets().add(getClass().getResource("dark-style.css").toExternalForm());
        primaryStage.setResizable(true);
        //------------------------------//
        ViewController viewController = fxmlLoader.getController();
        viewController.setViewModel(viewModel);
        viewModel.addObserver(viewController);
        //------------------------------//
        primaryStage.setScene(scene);
        SetStageCloseEvent(primaryStage, viewController);
        primaryStage.show();
        //------------------------------//
    }

    private void SetStageCloseEvent(Stage primaryStage, ViewController viewController) {
        primaryStage.setOnCloseRequest(windowEvent -> {
            viewController.exitCorrectly();
            windowEvent.consume();
        });
    }
}