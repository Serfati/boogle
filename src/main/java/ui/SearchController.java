package ui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ranker.Searcher;
import view_model.ViewModel;

import java.io.File;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

import static ui.ListToPDF.Orientation.PORTRAIT;
import static ui.UIController.openGoogle;

public class SearchController implements Observer, Initializable {

    private final static Logger LOGGER = LogManager.getLogger(SearchController.class.getName());
    ObservableList<Searcher.ShowResultRecord> recordsToPDF;
    public TabPane tabManager;
    public TableView<Searcher.ShowResultRecord> table_showDic;
    public TableColumn<Searcher.ShowResultRecord, String> tableCol_1;
    public TableColumn<Searcher.ShowResultRecord, String> tableCol_2;
    public TableColumn<Searcher.ShowResultRecord, Number> tableCol_3;
    public TableColumn numberCol;
    public StackPane rootContainer;
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
    private JFXTextField queryField_txt;
    @FXML
    private JFXTextField outputField_txt;
    @FXML
    private JFXCheckBox semantic_checkbox;
    @FXML
    public JFXButton btn_show_data;

    private ViewModel viewModel;

    /**
     * constructor of view, connect the view to the viewModel
     *
     * @param viewModel the view model of the MVVM
     */
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        LOGGER.log(Level.INFO, "Boogle Search page opened.");
        table_showDic.setRowFactory(tv -> {
            TableRow<Searcher.ShowResultRecord> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Searcher.ShowResultRecord rowData = row.getItem();
                    System.out.println("Opening "+rowData.getDocumentProperty());
                    show5words(rowData.getDocumentProperty().toString());
                }
            });
            return null;
        });
        numberCol = new TableColumn("#");
        numberCol.setCellValueFactory(
                (Callback<TableColumn.CellDataFeatures<Searcher.ShowResultRecord, String>, ObservableValue<String>>)
                        p -> new ReadOnlyObjectWrapper(table_showDic.getItems().indexOf(p.getValue())+""));
        numberCol.setSortable(false);
    }

    /**
     * This function starts the process of parse and index the dictionary
     */
    public void onSearchBoogleClick() {
        if (!google_txt.getText().equalsIgnoreCase("")) {
            openGoogle(google_txt.getText());
            AlertMaker.showSimpleAlert("Congratulation", "Good Choice");
        }
        if (outputField_txt.getText().equals("") || queryField_txt.getText().equals(""))// check if the paths are not empty
            AlertMaker.showErrorMessage("Error", "Must fill a query first");
        else {
            String query = !queryField_txt.getText().equals("") ? queryField_txt.getText() : outputField_txt.getText();
            viewModel.onSearchBoogleClick(UIController.txtfld_output_location.toString(), query, outputField_txt.getText(), semantic_checkbox.isSelected(), stem_checkbox.isSelected(), offline_checkbox.isSelected()); //transfer to the view Model
            btn_boogle_search.setText("DONE!");
            btn_boogle_search.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                SingleSelectionModel<Tab> selectionModel = tabManager.getSelectionModel();
                selectionModel.select(1);
            });
        }
    }

    @FXML
    public void saveQueryResults() {
        if (outputField_txt.getText().equals("")) {
            AlertMaker.showErrorMessage(Alert.AlertType.ERROR.name(), "You didnt choose output location");
            return;
        }
        boolean isWrite = viewModel.writeRes(outputField_txt.getText());
        if (isWrite)
            AlertMaker.showErrorMessage(Alert.AlertType.INFORMATION.name(), "results saved!");
        else AlertMaker.showErrorMessage(Alert.AlertType.ERROR.name(), "Something went wrong");
    }

    @FXML
    private void handleDatabaseExportAction() {
        ListToPDF l2PDF = new ListToPDF();
        l2PDF.doPrintToPdf(recordsToPDF, outputField_txt.getText(), PORTRAIT);
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
                String[] toUpdate = (String[]) arg;
                switch(toUpdate[0]) {
                    case "Successful": // if we received a successful message from the model
                        AlertMaker.showSimpleAlert(Alert.AlertType.INFORMATION.name(), toUpdate[1]);
                        if (toUpdate[1].equalsIgnoreCase("The folder is clean now"))
                            btn_boogle_search.setDisable(true);
                        if (toUpdate[1].substring(0, toUpdate[1].indexOf(" ")).equals("Dictionary"))
                            btn_show_data.setDisable(false);
                        break;
                    case "Fail":
                        if (toUpdate[1].equals("could not find one or more dictionaries"))
                            AlertMaker.showErrorMessage(Alert.AlertType.ERROR.name(), toUpdate[1]);
                        break;
                }
            } else if (arg instanceof ObservableList) {
                ObservableList l = (ObservableList) arg;
                if (!l.isEmpty() && l.get(0) instanceof Searcher.ShowResultRecord)
                    showData((ObservableList<Searcher.ShowResultRecord>) arg);
                btn_export_pdf.setDisable(false);
                btn_show_data.setDisable(false);
                btn_boogle_search.setDisable(true);
            }
        }
    }

    /**
     * returns the 5 Entities of a document
     *
     * @param docName document name
     */
    private void show5words(String docName) {
        StringBuilder fiveIdentities = viewModel.showFiveEntities(docName);
        if (fiveIdentities.length() < 1)
            fiveIdentities.append("No identities found");
        AlertMaker.showSimpleAlert(docName+" Top entities", fiveIdentities.toString());
    }

    /**
     * transfers a request to show the dictionary of the current indexing
     */
    public void showDataClick() {
        viewModel.showData();
    }

    /**
     * shows an observable list that contains all the data about the current indexing: Term and TF
     *
     * @param records all the data about the current indexing
     */
    private void showData(ObservableList<Searcher.ShowResultRecord> records) {
        this.recordsToPDF = records;
        if (records != null) {
            tableCol_1.setCellValueFactory(cellData -> cellData.getValue().getDocumentProperty());
            tableCol_2.setCellValueFactory(cellData -> cellData.getValue().getTopFiveProperty());
            tableCol_3.setCellValueFactory(cellData -> cellData.getValue().getScoreProperty());
            table_showDic.setItems(records);
        }
        btn_show_data.setDisable(false);
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
}