package ui;

import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.Stage;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.net.URL;
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
    private JFXSpinner progressSpinner;

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

    @FXML
    private void saveQuery(ActionEvent event) {
    }

    @FXML
    private void handleDatabaseExportAction(ActionEvent event) {
    }
}