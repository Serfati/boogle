package View;

import Controller.Controller;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;


public class View implements IView {

    @FXML
    public TextField txtfld_corpus_location;
    @FXML
    public TextField txtfld_stopwords_location;
    @FXML
    public TextField txtfld_output_location;
    public Button btn_corpus_browse;
    public Button btn_stopwords_browse;
    public Button btn_output_browse;
    public Button btn_reset;
    public Button btn_load_dictionary;
    public Button btn_display_dictionary;
    public Button view_search;
    public ChoiceBox choiceBox_languages;
    public CheckBox chkbox_use_stemming;
    public CheckBox chkbox_memory_saver;
    public BorderPane root_pane;
    private Controller controller;

    public View(Controller controller) {
        this.controller = controller;
    }

    @FXML
    private void initialize() {
    }

    @Override
    public void setController(Controller controller) {
        this.controller = controller;
    }

    public boolean isUseStemming() {
        return chkbox_use_stemming.isSelected();
    }

    public void browseCorpusLocation(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Corpus Location");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        File corpusDir = directoryChooser.showDialog(btn_corpus_browse.getScene().getWindow());
        if (null != corpusDir) { //directory chosen
            txtfld_corpus_location.TextProperty().setValue(corpusDir.getAbsolutePath());
        }
    }

    public void browseOutputLocation(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Output Location");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        File corpusDir = directoryChooser.showDialog(btn_output_browse.getScene().getWindow());
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
            txtfld_stopwords_location..setValue(corpusDir.getAbsolutePath());
        }
    }

    public void reset(ActionEvent actionEvent) {
        btn_display_dictionary.setEnabled(false);
        choiceBox_languages.setDisable(true);
    }
}