package ui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ranker.ResultDisplay;
import ranker.ResultDisplay.QueryDisplay;
import view_model.ViewModel;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

import static ui.ListToPDF.Orientation.PORTRAIT;
import static ui.UIController.openGoogle;

public class SearchController implements Observer, Initializable {

    private final static Logger LOGGER = LogManager.getLogger(SearchController.class.getName());
    private static ViewModel viewModel;
    public TabPane tabManager;

    public TableView<QueryDisplay> table_showDocs;
    public TableColumn<QueryDisplay, String> tableCol_docs;

    public TableView<ResultDisplay> table_showResults;
    public TableColumn<ResultDisplay, String> tableCol_query;

    public JFXButton btn_export_pdf;
    public JFXButton btn_boogle_search;
    public JFXTextField google_txt;
    public JFXCheckBox stem_checkbox;
    public JFXSpinner progressSpinner;
    public JFXCheckBox offline_checkbox;
    public JFXTextField DONE_txt;
    public JFXTextField queryField_txt;
    public JFXTextField corpusField_txt;
    public JFXCheckBox semantic_checkbox;
    public JFXButton btn_show_data;
    ObservableList<ResultDisplay> recordsToPDF;

    /**
     * constructor of view, connect the view to the viewModel
     *
     * @param viewModel the view model of the MVVM
     */
    public void setViewModel(ViewModel viewModel) {
        SearchController.viewModel = viewModel;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        LOGGER.log(Level.INFO, "Search Tab Switch");
    }

    /**
     * This function starts the process of parse and index the dictionary
     */
    public void onSearchBoogleClick() {
        if (!google_txt.getText().equalsIgnoreCase("")) {
            openGoogle(google_txt.getText());
            AlertMaker.showSimpleAlert("Congratulation", "Good Choice");
        }
        if (corpusField_txt.getText().equals("") || queryField_txt.getText().equals("")) // check if the paths are not empty
            AlertMaker.showErrorMessage("Error", "Must fill a query first");
        else {
            String query = !queryField_txt.getText().equals("") ? queryField_txt.getText() : corpusField_txt.getText();
            tabManager.getSelectionModel().select(1);
            viewModel.onSearchBoogleClick(corpusField_txt.getText(), query, corpusField_txt.getText(), stem_checkbox.isSelected(), semantic_checkbox.isSelected(), offline_checkbox.isSelected()); //transfer to the view Model
        }
    }

    @FXML
    private void handleDatabaseExportAction() {
        ListToPDF l2PDF = new ListToPDF();
        l2PDF.doPrintToPdf(recordsToPDF, corpusField_txt.getText(), PORTRAIT);
    }

    public void browseQueryClick(MouseEvent event) {
        if (event.getClickCount() == 2 && !event.isConsumed()) {
            event.consume();
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Query file Location");
            fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
            File corpusDir = fileChooser.showOpenDialog(new Stage());
            if (null != corpusDir)  //directory chosen
                queryField_txt.setText(corpusDir.getAbsolutePath());
        }
    }

    public void browseOutputClick(MouseEvent event) {
        if (event.getClickCount() == 2 && !event.isConsumed()) {
            event.consume();
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Output Location");
            directoryChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
            File corpusDir = directoryChooser.showDialog(new Stage());
            if (null != corpusDir)  //file chosen
                corpusField_txt.setText(corpusDir.getAbsolutePath());
        }
    }

    public void update(Observable o, Object arg) {
        if (o == viewModel && arg instanceof ObservableList) { // a show dictionary operation was finished and can be shown on display
            List l = (ObservableList) arg;
            if (!l.isEmpty() && l.get(0) instanceof ResultDisplay)
                showQueryResults((ObservableList<ResultDisplay>) l);
        }
    }

    /**
     * returns the 5 Entities of a document
     *
     * @param docName document name
     */
    private void showFiveEntities(String docName) {
        String fiveIdentities = viewModel.showFiveEntities(docName);
        if (fiveIdentities.equals(""))
            System.out.println("No identities found");
    }

    /**
     * show the query number that were searched
     *
     * @param results query numbers
     */
    private void showQueryResults(ObservableList<ResultDisplay> results) {
        System.out.println("size of result is "+results.size());
        tableCol_query.setCellValueFactory(cellData -> cellData.getValue().sp_queryIDProperty());
        table_showResults.setItems(results);
            table_showResults.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (observable != null && table_showResults.getItems().size() > 0)
                    showQueryResult((ObservableValue<ResultDisplay>) observable);
            });
    }

    /**
     * show the docs relevant for each query
     * @param observable the
     */
    private void showQueryResult(ObservableValue<ResultDisplay> observable) {
        if (observable != null) {
            ObservableList<QueryDisplay> observableList = FXCollections.observableList(observable.getValue().getDocNames());
            tableCol_docs.setCellValueFactory(cellData -> cellData.getValue().sp_docNameProperty());
            table_showDocs.setItems(observableList);
            table_showDocs.getSelectionModel().selectedItemProperty().addListener((observable1, oldValue, newValue) -> {
                if (observable1 != null && newValue != null)
                    showFiveEntities(observable1.getValue().getSp_docName());
            });
        }
    }

    @FXML
    public void saveResults() {
        if (corpusField_txt.getText().equals("")) {
            AlertMaker.showErrorMessage(Alert.AlertType.ERROR.name(), "You didnt choose output location");
            return;
        }
        boolean isWrite = viewModel.writeRes(corpusField_txt.getText());
        if (isWrite)
            AlertMaker.showSimpleAlert(Alert.AlertType.INFORMATION.name(), "results saved ->\n "+corpusField_txt.getText());
        else AlertMaker.showErrorMessage(Alert.AlertType.ERROR.name(), "Something went wrong");
    }

    /**
     * transfers a request to show the dictionary of the current indexing
     */
    public void showDataClick() {
        viewModel.showDataClick();
    }
}