package View;

import ViewModel.MyViewModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class MyViewController {

    public Label lbl_statusBar;
    public MenuItem save_MenuItem;
    public MenuItem solve_MenuItem;


    public javafx.scene.image.ImageView icon_make;
    @FXML
    private java.awt.TextField txtfld_corpus_location;
    private java.awt.TextField txtfld_stopwords_location;
    public javafx.scene.image.ImageView icon_sound;
    public javafx.scene.image.ImageView icon_partSolution;
    public javafx.scene.image.ImageView icon_fullSolution;
    private java.awt.TextField txtfld_output_location;
    public javafx.scene.image.ImageView icon_zoomImageView;
    public javafx.scene.control.ScrollPane ScrollPane;
    public java.awt.Button btn_corpus_browse;
    public java.awt.Button btn_stopwords_browse;
    public java.awt.Button btn_output_browse;
    public java.awt.Button btn_reset;
    public java.awt.Button btn_load_dictionary;
    private java.awt.Button btn_display_dictionary;
    public java.awt.Button view_search;
    private ChoiceBox choiceBox_languages;
    private CheckBox chkbox_use_stemming;
    public CheckBox chkbox_memory_saver;
    public BorderPane root_pane;
    public ScrollPane scrollPane;
    private String soundOnOff;
    private Stage stageNewGameController;
    private MyViewModel myViewModel;

    public void initialize(URL location, ResourceBundle resources) {
        initLogo();
    }

    private void initLogo() {

    }

    public void scrollInOut(ScrollEvent scrollEvent) {
    }

    public void KeyPressed(KeyEvent keyEvent) {

    }

    public void setViewModel(MyViewModel myViewModel) {
        this.myViewModel = myViewModel;
    }

    public boolean isUseStemming() {
        return chkbox_use_stemming.isSelected();
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
        fileChooser.setTitle("Output Location");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        File corpusDir = fileChooser.showOpenDialog(new Stage());
        if (null != corpusDir) { //directory chosen
            txtfld_stopwords_location.setText(corpusDir.getAbsolutePath());
        }
    }

    public void reset(ActionEvent actionEvent) {
        btn_display_dictionary.setEnabled(false);
        choiceBox_languages.setDisable(true);
    }

    public void exitButton() {
        exitCorrectly();
    }

    private void exitCorrectly() {
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
}
