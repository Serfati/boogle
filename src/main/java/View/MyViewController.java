package View;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Optional;

public class MyViewController {
    public Label lbl_characterRow;
    public Label lbl_characterColumn;
    public Label lbl_statusBar;
    public MenuItem save_MenuItem;
    public MenuItem solve_MenuItem;


    @FXML
    public java.awt.TextField txtfld_corpus_location;
    public java.awt.TextField txtfld_stopwords_location;
    public java.awt.TextField txtfld_output_location;
    public javafx.scene.image.ImageView icon_sound;
    public javafx.scene.image.ImageView icon_partSolution;
    public javafx.scene.image.ImageView icon_fullSolution;
    public javafx.scene.image.ImageView icon_makeNewMaze;
    public javafx.scene.image.ImageView icon_zoomImageView;
    public javafx.scene.control.ScrollPane ScrollPane;
    public java.awt.Button btn_corpus_browse;
    public java.awt.Button btn_stopwords_browse;
    public java.awt.Button btn_output_browse;
    public java.awt.Button btn_reset;
    public java.awt.Button btn_load_dictionary;
    public java.awt.Button btn_display_dictionary;
    public java.awt.Button view_search;
    public ChoiceBox choiceBox_languages;
    public CheckBox chkbox_use_stemming;
    public CheckBox chkbox_memory_saver;
    public BorderPane root_pane;
    public ScrollPane scrollPane;
    private String soundOnOff;
    private Stage stageNewGameController;

    public void scrollInOut(ScrollEvent scrollEvent) {
    }

    public void KeyPressed(KeyEvent keyEvent) {

    }


    public boolean isUseStemming() {
        return chkbox_use_stemming.isSelected();
    }

    public void browseCorpusLocation(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Corpus Location");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        File corpusDir = directoryChooser.showDialog(btn_corpus_browse.getLabel());
        if (null != corpusDir) { //directory chosen
            txtfld_corpus_location.setText(corpusDir.getAbsolutePath());
        }
    }

    public void browseOutputLocation(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Output Location");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        File corpusDir = directoryChooser.showDialog(btn_output_browse);
        if (null != corpusDir) { //directory chosen
            txtfld_output_location.setText(corpusDir.getAbsolutePath());
        }
    }

    public void browseStopwordsLocation(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Output Location");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        File corpusDir = fileChooser.showOpenDialog(btn_stopwords_browse.getScene().getWindow());
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
        if (result.get() == leaveButton) {
            // ... user chose to Leave
            // Close program
            //myViewModel.closeModel();
            Platform.exit();
        } else
            // ... user chose to CANCEL or closed the dialog
            alert.close();
    }
}
