package ui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.PopupWindow;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ranker.Searcher;
import view_model.ViewModel;

import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

public class SearchController implements Observer, Initializable {

    private final static Logger LOGGER = LogManager.getLogger(SearchController.class.getName());
    public TabPane tabManager;
    public TableView<Searcher.ShowResultRecord> table_showDic;
    public TableColumn<Searcher.ShowResultRecord, String> tableCol_1;
    public TableColumn<Searcher.ShowResultRecord, String> tableCol_2;
    public TableColumn<Searcher.ShowResultRecord, Number> tableCol_3;
    public TableColumn numberCol;
    public StackPane rootContainer;
    public JFXButton btn_save_results;
    public JFXButton btn_export_pdf;
    public JFXButton btn_boogle_search;
    @FXML
    public JFXTextField entite_text;
    @FXML
    public JFXCheckBox stem_checkbox;
    public JFXButton btn_query_browse;
    public JFXSpinner progressSpinner;
    @FXML
    private JFXTextField queryField_txt;
    @FXML
    private JFXTextField outputField_txt;
    @FXML
    private JFXTextField goToOutputs_txt;
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

        numberCol = new TableColumn("#");
        numberCol.setCellValueFactory(
                (Callback<TableColumn.CellDataFeatures<Searcher.ShowResultRecord, String>, ObservableValue<String>>)
                        p -> new ReadOnlyObjectWrapper(table_showDic.getItems().indexOf(p.getValue())+""));
        numberCol.setSortable(false);
        initDefaultValues();
    }

    /**
     * This function starts the process of parse and index the dictionary
     */
    public void onSearchBoogleClick() {
        if (outputField_txt.getText().equals("") || queryField_txt.getText().equals(""))// check if the paths are not empty
            AlertMaker.showErrorMessage("Error", "Must fill a query first");
        else {
            String query = !queryField_txt.getText().equals("") ? queryField_txt.getText() : outputField_txt.getText();
            viewModel.onSearchBoogleClick(query, goToOutputs_txt.getText(), semantic_checkbox.isSelected(), stem_checkbox.isSelected()); //transfer to the view Model
            btn_boogle_search.setText("DONE!");
            btn_boogle_search.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                SingleSelectionModel<Tab> selectionModel = tabManager.getSelectionModel();
                selectionModel.select(1);
            });
        }
    }

    /**
     * transfers a request to show the dictionary of the current indexing
     */
    public void showDataClick() {
        viewModel.showDataClick();
    }

    /**
     * shows an observable list that contains all the data about the current indexing: Term and TF
     *
     * @param records all the data about the current indexing
     */
    private void showData(ObservableList<Searcher.ShowResultRecord> records) {
        if (records != null) {
            tableCol_1.setCellValueFactory(cellData -> cellData.getValue().getDocumentProperty());
            tableCol_2.setCellValueFactory(cellData -> cellData.getValue().getTopFiveProperty());
            tableCol_3.setCellValueFactory(cellData -> cellData.getValue().getScoreProperty());
            table_showDic.setItems(records);
        }
        btn_show_data.setDisable(false);
    }

    @FXML
    private void handleSaveButtonAction(ActionEvent event) {
        int ndays = Integer.parseInt(queryField_txt.getText());
        float fine = Float.parseFloat(outputField_txt.getText());
    }

    public void browseQueryClick(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Query file Location");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        File corpusDir = fileChooser.showOpenDialog(new Stage());
        if (null != corpusDir)  //directory chosen
            queryField_txt.setText(corpusDir.getAbsolutePath());
    }

    public void browseOutputClick(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Output Location");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        File corpusDir = directoryChooser.showDialog(new Stage());
        if (null != corpusDir)  //directory chosen
            outputField_txt.setText(corpusDir.getAbsolutePath());
    }

    private Stage getStage() {
        return ((Stage) queryField_txt.getScene().getWindow());
    }

    private void initDefaultValues() {
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
                if (toUpdate[0].equals("Fail")) {
                    if (toUpdate[1].equals("could not find one or more dictionaries"))
                        AlertMaker.showErrorMessage(Alert.AlertType.ERROR.name(), toUpdate[1]);
                } else if (toUpdate[0].equals("Successful")) {// if we received a successful message from the model
                    AlertMaker.showSimpleAlert(Alert.AlertType.INFORMATION.name(), toUpdate[1]);
                    if (toUpdate[1].equalsIgnoreCase("The folder is clean now"))
                        btn_boogle_search.setDisable(true);
                    if (toUpdate[1].substring(0, toUpdate[1].indexOf(" ")).equals("Dictionary"))
                        btn_show_data.setDisable(false);
                }
            } else if (arg instanceof ObservableList) {
                ObservableList l = (ObservableList) arg;
                if (!l.isEmpty() && l.get(0) instanceof Searcher.ShowResultRecord)
                    showData((ObservableList<Searcher.ShowResultRecord>) arg);
            } else if (arg instanceof double[]) {
                double[] res = (double[]) arg;
                btn_show_data.setDisable(false);
                btn_boogle_search.setDisable(true);
            }
        }
    }
}