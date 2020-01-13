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
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Model;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ranker.QueryDisplay;
import ranker.ResultDisplay;
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
    @FXML
    public JFXTextField google_txt;
    @FXML
    public JFXCheckBox stem_checkbox;
    public JFXSpinner progressSpinner;
    public JFXCheckBox offline_checkbox;
    public JFXTextField DONE_txt;
    @FXML
    public JFXTextField queryField_txt;
    @FXML
    public JFXTextField outputField_txt;
    @FXML
    public JFXCheckBox semantic_checkbox;
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
        LOGGER.log(Level.INFO, "Boogle Search page opened.");
    }

    /**
     * This function starts the process of parse and index the dictionary
     */
    public void onSearchBoogleClick() {
        viewModel.getClass();
        clearTables();
        if (!google_txt.getText().equalsIgnoreCase("")) {
            openGoogle(google_txt.getText());
            AlertMaker.showSimpleAlert("Congratulation", "Good Choice");
        }
        if (outputField_txt.getText().equals("") || queryField_txt.getText().equals(""))// check if the paths are not empty
            AlertMaker.showErrorMessage("Error", "Must fill a query first");
        else {
            String query = !queryField_txt.getText().equals("") ? queryField_txt.getText() : outputField_txt.getText();
            viewModel.onSearchBoogleClick("/home/serfati/Desktop/Out", query, outputField_txt.getText(), semantic_checkbox.isSelected(), stem_checkbox.isSelected(), offline_checkbox.isSelected()); //transfer to the view Model
        }
    }

    @FXML
    private void handleDatabaseExportAction() {
        ListToPDF l2PDF = new ListToPDF();
        l2PDF.doPrintToPdf(recordsToPDF, outputField_txt.getText(), PORTRAIT);
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
                outputField_txt.setText(corpusDir.getAbsolutePath());
        }
    }

    /**
     * a function that gets called when an observer has raised a flag for something that changed
     *
     * @param o   - who changed
     * @param arg - the change
     */
    public void update(Observable o, Object arg) {
        if (o == viewModel) {
            if (arg instanceof String[]) {
//                String[] toUpdate = (String[]) arg;
//                switch(toUpdate[0]) {
//                    case "Successful": // if we received a successful message from the model
//                        AlertMaker.showSimpleAlert(Alert.AlertType.INFORMATION.name(), toUpdate[1]);
//                        if (toUpdate[1].equalsIgnoreCase("The folder is clean now"))
//                            btn_boogle_search.setDisable(true);
//                        if (toUpdate[1].substring(0, toUpdate[1].indexOf(" ")).equals("Dictionary"))
//                            btn_show_data.setDisable(false);
//                        break;
//                    case "Fail":
//                        if (toUpdate[1].equals("could not find one or more dictionaries"))
//                            AlertMaker.showErrorMessage(Alert.AlertType.ERROR.name(), toUpdate[1]);
//                        break;
            }
        } else if (arg instanceof ObservableList) {
            List l = (ObservableList) arg;
            if (!l.isEmpty() && l.get(0) instanceof ResultDisplay)
                showQueryResults((ObservableList<ResultDisplay>) l);
            btn_export_pdf.setDisable(false);
            btn_boogle_search.setDisable(true);

            btn_boogle_search.setText("DONE!");

            btn_boogle_search.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                SingleSelectionModel<Tab> selectionModel = tabManager.getSelectionModel();
                selectionModel.select(1);
            });
        }
    }

    /**
     * clears table when a new query is entered
     */
    private void clearTables() {
        table_showResults.getItems().clear();
        table_showDocs.getItems().clear();
    }

    public void show5words(String docName) {
        if (Model.documentDictionary.containsKey(docName)) {
            try {
                Model.documentDictionary.get(docName).get5words();
            } catch(Exception e) {
                System.out.println(docName);
            }
        }
    }

    /**
     * show the query number that were searched
     *
     * @param results query numbers
     */
    private void showQueryResults(ObservableList<ResultDisplay> results) {
        if (results != null) {
            tableCol_query.setCellValueFactory(cellData -> cellData.getValue().sp_queryIDProperty());
            table_showResults.setItems(results);
            table_showResults.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (observable != null && table_showResults.getItems().size() > 0)
                    showQueryResult((ObservableValue<ResultDisplay>) observable);
            });
        }
    }

    /**
     * show the docs relevant for each query
     *
     * @param observable the
     */
    private void showQueryResult(ObservableValue<ResultDisplay> observable) {

        if (observable != null) {
            ObservableList<QueryDisplay> observableList = FXCollections.observableList(observable.getValue().getDocNames());
            tableCol_docs.setCellValueFactory(cellData -> cellData.getValue().sp_docNameProperty());
            table_showDocs.setItems(observableList);
            table_showDocs.getSelectionModel().selectedItemProperty().addListener((observable1, oldValue, newValue) -> {
                if (observable1 != null && newValue != null)
                    show5words(observable1.getValue().getSp_docName());
            });
        }
    }

    @FXML
    public void saveResults() {
        if (outputField_txt.getText().equals("")) {
            AlertMaker.showErrorMessage(Alert.AlertType.ERROR.name(), "You didnt choose output location");
            return;
        }
        boolean isWrite = viewModel.writeRes(outputField_txt.getText());
        if (isWrite)
            AlertMaker.showErrorMessage(Alert.AlertType.INFORMATION.name(), "results saved!");
        else AlertMaker.showErrorMessage(Alert.AlertType.ERROR.name(), "Something went wrong");
    }

}