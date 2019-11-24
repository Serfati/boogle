package View;

import ViewModel.MyViewModel;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.ResourceBundle;

public class MyViewController implements IView, Observer, Initializable {

    //IMAGES
    public ImageView icon_startSearch;
    public ImageView icon_partSolution;
    public ImageView icon_fullSolution;
    public ImageView icon_makeNewMaze;
    public ImageView icon_zoomImageView;
    public ImageView icon_sound;

    public Label lbl_statusBar;
    public MenuItem save_MenuItem;
    public MenuItem solve_MenuItem;

    @FXML
    public TextField txtfld_corpus_location;
    @FXML
    public TextField txtfld_stopwords_location;
    @FXML
    public TextField txtfld_output_location;

    public ScrollPane ScrollPane;
    public Button btn_corpus_browse;
    public Button btn_stopwords_browse;
    public Button btn_output_browse;
    public Button btn_reset;
    public Button btn_load_dictionary;
    public Button btn_display_dictionary;
    public Button view_search;
    public Button btn_genereate_index;
    @FXML
    private ChoiceBox choiceBox_languages;
    @FXML
    private CheckBox chkbox_use_stemming;
    public CheckBox chkbox_memory_saver;
    public BorderPane root_pane;
    public ScrollPane scrollPane;
    private String soundOnOff;
    private Stage stageNewGameController;
    private MyViewModel myViewModel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initImages();
        Tooltip tooltip = new Tooltip();
        tooltip.setText("");
        chkbox_memory_saver.setTooltip(tooltip);
        view_search.setDisable(true);
    }


    private void initImages() {
        File file = new File("resources/icon_new.png");
        Image image = new Image(file.toURI().toString());
        icon_startSearch.setImage(image);
        setClick(icon_startSearch);

        file = new File("resources/icon_new.png");
        image = new Image(file.toURI().toString());
        icon_makeNewMaze.setImage(image);
        setClick(icon_makeNewMaze);

        file = new File("resources/icon_new.png");
        image = new Image(file.toURI().toString());
        icon_partSolution.setImage(image);
        setClick(icon_partSolution);

        file = new File("resources/icon_new.png");
        image = new Image(file.toURI().toString());
        icon_fullSolution.setImage(image);
        setClick(icon_fullSolution);

        file = new File("resources/icon_new.png");
        image = new Image(file.toURI().toString());
        icon_zoomImageView.setImage(image);
        setClick(icon_zoomImageView);

        file = new File("resources/icon_new.png");
        image = new Image(file.toURI().toString());
        icon_sound.setImage(image);
        setClick(icon_sound);

    }

    private void setClick(javafx.scene.image.ImageView icon) {
        icon.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            help();
            event.consume();
        });
    }


    public void scrollInOut(ScrollEvent scrollEvent) {
    }

    public void KeyPressed(KeyEvent keyEvent) {
        if (!myViewModel.isFinish())
            exitCorrectly();
        else
            lbl_statusBar.setText("If you want to search again just type");
        keyEvent.consume();
    }

    public void setViewModel(MyViewModel myViewModel) {
        this.myViewModel = myViewModel;
    }

    public boolean isUseStemming() {
        return chkbox_use_stemming.isSelected();
    }

    public void generateIndex(ActionEvent actionEvent) {

    }

    public void browseCorpusLocation(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Corpus Location");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        File corpusDir = directoryChooser.showDialog(new Stage());
        if (null != corpusDir) { //directory chosen
            txtfld_corpus_location.setText(corpusDir.getAbsolutePath());
        }
    }

    public void browseOutputLocation(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Output Location");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        File corpusDir = directoryChooser.showDialog(new Stage());
        if (null != corpusDir) { //directory chosen
            txtfld_output_location.setText(corpusDir.getAbsolutePath());
        }
    }

    public void browseStopwordsLocation(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Stopwords Location");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        File corpusDir = fileChooser.showOpenDialog(new Stage());
        if (null != corpusDir) { //directory chosen
            txtfld_stopwords_location.setText(corpusDir.getAbsolutePath());
        }
    }

    private void handleNewDictionary(Alert result) {
        if (result.getAlertType() == Alert.AlertType.ERROR) {
            result.showAndWait();
        } else {
            result.show();
            btn_reset.setDisable(false);
            btn_display_dictionary.setDisable(false);
            view_search.setDisable(false);
        }
    }

    public void exitButton() {
        exitCorrectly();
    }

    void exitCorrectly() {
        Alert alert = new Alert(Alert.AlertType.NONE);
        ButtonType leaveButton = new ButtonType("Leave", ButtonBar.ButtonData.YES);
        ButtonType stayButton = new ButtonType("Stay", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(stayButton, leaveButton);
        alert.setContentText("Are you sure you want to exit??");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == leaveButton) {
            Platform.exit();
        } else
            alert.close();
    }

    public void help() {
        Stage helpStage = new Stage();
        helpStage.setAlwaysOnTop(true);
        helpStage.setResizable(false);
        helpStage.setTitle("Help Window");

        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("Help.fxml"));
        } catch(IOException e) {
            e.printStackTrace();
            showAlert();
        }
        helpStage.setTitle("Help");
        assert root != null;
        Scene scene = new Scene(root, 520, 495);
        scene.getStylesheets().add(getClass().getResource("ViewStyle.css").toExternalForm());
        helpStage.setScene(scene);
        helpStage.initModality(Modality.WINDOW_MODAL);
        helpStage.show();
    }

    public void About() {
        Stage aboutStage = new Stage();
        aboutStage.setAlwaysOnTop(true);
        aboutStage.setResizable(false);
        aboutStage.setTitle("About Window");

        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("About.fxml"));
        } catch(IOException e) {
            showAlert();
        }
        aboutStage.setTitle("About");
        assert root != null;
        Scene scene = new Scene(root, 600, 400);
        scene.getStylesheets().add(getClass().getResource("ViewStyle.css").toExternalForm());
        aboutStage.setScene(scene);
        aboutStage.initModality(Modality.APPLICATION_MODAL);
        aboutStage.show();
    }

    private void showAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setGraphic(null);
        alert.setTitle("Error Alert");
        alert.setContentText("Exception!");
        alert.show();
    }

    public void reset(ActionEvent actionEvent) {
        btn_display_dictionary.setDisable(true);
        choiceBox_languages.setDisable(true);
    }

    @Override
    public void update(Observable o, Object arg) {

    }
}
