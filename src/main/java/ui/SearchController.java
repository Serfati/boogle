package ui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import indexer.InvertedIndex;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.PopupWindow;
import javafx.stage.Stage;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;


public class SearchController implements Initializable {

    private final static Logger LOGGER = LogManager.getLogger(SearchController.class.getName());

    @FXML
    private JFXTextField nDaysWithoutFine;
    @FXML
    private JFXTextField finePerDay;
    @FXML
    private JFXPasswordField emailPassword;
    @FXML
    private JFXCheckBox sslCheckbox;
    @FXML
    public TableView<InvertedIndex.ShowDictionaryRecord> table_showDic;
    public TableColumn<InvertedIndex.ShowDictionaryRecord, String> tableCol_term;
    public TableColumn<InvertedIndex.ShowDictionaryRecord, Number> tableCol_count;
    @FXML
    public JFXButton btn_show_data;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initDefaultValues();
    }

    @FXML
    private void handleSaveButtonAction(ActionEvent event) {
        int ndays = Integer.parseInt(nDaysWithoutFine.getText());
        float fine = Float.parseFloat(finePerDay.getText());
    }

    private Stage getStage() {
        return ((Stage) nDaysWithoutFine.getScene().getWindow());
    }

    private void initDefaultValues() {
    }

    @FXML
    private void handleTestAction(ActionEvent event) {
    }

    /**
     * @param records all the data about the current indexing
     */
    private void showData(ObservableList<InvertedIndex.ShowDictionaryRecord> records) {
        if (records != null) {
            tableCol_term.setCellValueFactory(cellData -> cellData.getValue().getTermProperty());
            tableCol_count.setCellValueFactory(cellData -> cellData.getValue().getCountProperty());
            table_showDic.setItems(records);
        }
        btn_show_data.setDisable(false);
    }

    @FXML
    public void saveQueryResults(ActionEvent event) {
        int[] choose = {0};
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Save Dictionary");
        alert.setContentText("Do you want to save dictionary?");
        ButtonType okButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(okButton, cancelButton);
        alert.showAndWait().ifPresent(type -> {
            if (type == okButton) //Current
                choose[0] = 1;
        });
        if (choose[0] == 0) {
            LOGGER.log(Level.INFO, "Save was canceled");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a directory to save the Dictionary in");
        File filePath = new File("./dictionaries/");
        if (!filePath.exists())
            filePath.mkdir();
        fileChooser.setInitialDirectory(filePath);
        LocalDateTime myDateObj = LocalDateTime.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM mm:HH");
        String formattedDate = myDateObj.format(myFormatObj);
        fileChooser.setInitialFileName("myDictionary;"+formattedDate);
        File file = fileChooser.showSaveDialog(new PopupWindow() {
        });
        if (file != null)
            LOGGER.log(Level.INFO, "Current dictionary saved");
        event.consume();
    }

    @FXML
    private void handleDatabaseExportAction(ActionEvent event) {
    }
}