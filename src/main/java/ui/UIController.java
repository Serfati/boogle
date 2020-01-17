package ui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import indexer.InvertedIndex;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.*;
import org.apache.commons.lang3.SystemUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import view_model.ViewModel;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;

public class UIController implements IView, Observer, Initializable {
    private final static Logger LOGGER = LogManager.getLogger(UIController.class.getName());

    @FXML
    public ImageView saveIcon;
    public ImageView boogleLogo;
    public ImageView searchTab;
    public ImageView generateIndexIcon;
    public ImageView settings;
    @FXML
    public TableView<InvertedIndex.ShowDictionaryRecord> table_showDic;
    public TableColumn<InvertedIndex.ShowDictionaryRecord, String> tableCol_term;
    public TableColumn<InvertedIndex.ShowDictionaryRecord, Number> tableCol_count;

    public MenuItem save_MenuItem;
    public MenuItem load_MenuItem;

    @FXML
    public JFXTextField txtfld_corpus_location;
    public JFXTextField txtfld_stopwords_location;
    public JFXTextField txtfld_output_location;

    @FXML
    public JFXButton btn_corpus_browse;
    public JFXButton btn_show_dictionary;
    public JFXButton btn_generate_index;
    public JFXButton btn_stopwords_browse;
    public JFXButton btn_output_browse;
    public JFXButton btn_startOver;
    public JFXButton btn_load_dictionary;
    public Label lbl_totalTime;
    public BorderPane rootPane;
    private ViewModel viewModel;
    @FXML
    private JFXToggleButton checkbox_use_stemming;
    SearchController searchController = new SearchController();


    /**
     * constructor of view, connect the view to the viewModel
     *
     * @param viewModel the view model of the MVVM
     */
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
        searchController.setViewModel(viewModel);
        viewModel.addObserver(searchController);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initImages();
    }

    private void initImages() {
        setBoogleClick();
        setGenerateIndexClick(generateIndexIcon);
        setSaveClick(saveIcon);
        setSettingsClick(settings);
        setSearchTabClick(searchTab);
    }

    /* WORKS ONLY ON - Windows OS */
    public static void openGoogle(String ask) {
        String OS = SystemUtils.OS_NAME;

        if (OS.startsWith("Windows")) {
            try {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE))
                    desktop.browse(new URL("https://www.google.com/search?q="+ask).toURI());
            } catch(IOException | URISyntaxException e1) {
                System.out.println("fail");
                e1.printStackTrace();
            }
        } else { /* UNIX platform - Ubuntu OS */
            try {
                new ProcessBuilder("x-www-browser", "https://www.google.com/search?q="+ask).start();
            } catch(IOException e) {
                System.out.println("fail");
                e.printStackTrace();
            }
        }
    }

    /* This function starts the process of parse and index the dictionary*/
    public void onStartClick() {
        if (txtfld_corpus_location.getText().equals("") || txtfld_output_location.getText().equals(""))// check if the paths are not empty
            AlertMaker.showErrorMessage("Error", "path can not be empty");
        else {
            viewModel.onStartClick(txtfld_corpus_location.getText(), txtfld_output_location.getText(), checkbox_use_stemming.isSelected()); //transfer to the view Model
        }
    }

    private void setGenerateIndexClick(ImageView icon) {
        icon.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (txtfld_corpus_location.getText().equals("") || txtfld_output_location.getText().equals(""))// check if the paths are not empty
                AlertMaker.showErrorMessage("Error", "path can not be empty");
            else
                viewModel.onStartClick(txtfld_corpus_location.getText(), txtfld_output_location.getText(), checkbox_use_stemming.isSelected()); //transfer to the view Model
            event.consume();
        });
    }

    private void setSettingsClick(ImageView icon) {
        icon.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            help();
            event.consume();
        });
    }

    private void setSaveClick(ImageView icon) {
        icon.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            saveDictionary(new ActionEvent());
            event.consume();
        });
    }

    private void setSearchTabClick(ImageView icon) {
        icon.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            searchView();
            event.consume();
        });
    }

    public void setBoogleClick() {
        boogleLogo.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> openGoogle("SISE"));
    }

    /**
     * transfers a request to show the dictionary of the current indexing
     */
    public void showDictionaryClick() {
        viewModel.showDictionary();
    }

    /**
     * shows an observable list that contains all the data about the current indexing: Term and TF
     *
     * @param records all the data about the current indexing
     */
    private void showDictionary(ObservableList<InvertedIndex.ShowDictionaryRecord> records) {
        if (records != null) {
            tableCol_term.setCellValueFactory(cellData -> cellData.getValue().getTermProperty());
            tableCol_count.setCellValueFactory(cellData -> cellData.getValue().getCountProperty());
            table_showDic.setItems(records);
        }
        btn_show_dictionary.setDisable(false);
    }

    public void browseCorpusClick(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Corpus Location");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        File corpusDir = directoryChooser.showDialog(new Stage());
        if (null != corpusDir)  //directory chosen
            txtfld_corpus_location.setText(corpusDir.getAbsolutePath());
    }

    public void browseOutputClick(ActionEvent actionEvent) {
        try {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Output Location");
            directoryChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
            File corpusDir = directoryChooser.showDialog(new Stage());
            if (null != corpusDir)  //directory chosen
                txtfld_output_location.setText(corpusDir.getAbsolutePath());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void browseStopwordsClick(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Stopwords file Location");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        File corpusDir = fileChooser.showOpenDialog(new Stage());
        if (null != corpusDir)  //directory chosen
            txtfld_stopwords_location.setText(corpusDir.getAbsolutePath());
    }

    public void exitButton() {
        exitCorrectly();
    }

    public void exitCorrectly() {
        List<JFXButton> controls = new ArrayList<>();
        JFXButton yes = new JFXButton("stay");
        JFXButton no = new JFXButton("Leave");
        controls.add(yes);
        controls.add(no);
        AlertMaker.showMaterialDialog(rootPane, rootPane, controls, "exit", "Are you sure?");

        Alert alert = new Alert(Alert.AlertType.NONE);
        AlertMaker.styleAlert(alert);
        ButtonType leaveButton = new ButtonType("Leave", ButtonBar.ButtonData.YES);
        ButtonType stayButton = new ButtonType("Stay", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(stayButton, leaveButton);
        alert.setContentText("Are you sure?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == leaveButton)
            Platform.exit();
        else
            alert.close();
        rootPane.setEffect(null);
    }

    private void searchView() {
        Stage searchStage = new Stage();
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("/search.fxml"));
        } catch(IOException e) {
            e.printStackTrace();
            showAlert();
        }
        searchStage.setTitle("BOOGLE");
        Scene scene = new Scene(root, 720, 456);
        scene.getStylesheets().add(getClass().getResource("/dark-style.css").toExternalForm());
        searchStage.setScene(scene);
        searchStage.initModality(Modality.WINDOW_MODAL);
        searchStage.show();
    }

    public void help() {
        Stage helpStage = new Stage();
        helpStage.setAlwaysOnTop(true);
        helpStage.setResizable(true);

        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("/help.fxml"));
        } catch(IOException e) {
            e.printStackTrace();
            showAlert();
        }
        helpStage.setTitle("Help");
        assert root != null;
        Scene scene = new Scene(root, 520, 495);
        scene.getStylesheets().add(getClass().getResource("/dark-style.css").toExternalForm());
        helpStage.setScene(scene);
        helpStage.initModality(Modality.WINDOW_MODAL);
        helpStage.show();
    }

    public void About() {
        Stage aboutStage = new Stage();
        aboutStage.setAlwaysOnTop(true);
        aboutStage.setResizable(false);

        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("/about.fxml"));
        } catch(IOException e) {
            showAlert();
        }
        aboutStage.setTitle("About us");
        assert root != null;
        Scene scene = new Scene(root, 530, 247);
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
                        btn_startOver.setDisable(true);
                    if (toUpdate[1].substring(0, toUpdate[1].indexOf(" ")).equals("Dictionary"))
                        btn_show_dictionary.setDisable(false);
                }
            } else if (arg instanceof ObservableList) { // a show dictionary operation was finished and can be shown on display
                List l = (ObservableList) arg;
                if (!l.isEmpty() && l.get(0) instanceof InvertedIndex.ShowDictionaryRecord)
                    showDictionary((ObservableList<InvertedIndex.ShowDictionaryRecord>) arg);
            } else if (arg instanceof double[]) {
                double[] res = (double[]) arg;
                AlertMaker.showSimpleAlert("Boogle Engine Analyze Summary",
                        "Number of Documents: "+res[0]+"\nTotal runtime complex: "
                                +res[2]+" minutes"+"\n Unique terms: "+res[1]);
                btn_show_dictionary.setDisable(false);
                btn_load_dictionary.setDisable(true);
                btn_startOver.setDisable(false);
                lbl_totalTime.setVisible(true);
                lbl_totalTime.setText("Runtime complexity: "+res[2]+" m");
            }
        }
    }

    /**
     * This function deletes all the contents of the destination path
     */
    public void onStartOverClick() {
        if (!txtfld_output_location.getText().equals("")) { // check if the user is sure he wants to delete the whole folder he chose
            ButtonType stay = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
            ButtonType leave = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure?", leave, stay);
            Optional<ButtonType> result = alert.showAndWait();
            if (stay != result.get()) {
                return;
            }
            btn_load_dictionary.setDisable(false);
            lbl_totalTime.setVisible(false);
            viewModel.onStartOverClick(txtfld_output_location.getText());
        } else
            AlertMaker.showErrorMessage(Alert.AlertType.ERROR.name(), "destination path is unreachable");
    }

    public void saveDictionary(ActionEvent event) {
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

    /**
     * transfers to the view model a load dictionary request
     */
    public void loadDictionary(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose a dictionary to load");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        File file = directoryChooser.showDialog(new PopupWindow() {
        });
        if (file != null && file.exists() && file.isDirectory()) {
            viewModel.loadDictionary(file.getAbsolutePath(), checkbox_use_stemming.isSelected());
            LOGGER.log(Level.INFO, "Loaded::"+file.getName());
        } else
            AlertMaker.showErrorMessage("Invalid", "Please choose a vaild destination");
        event.consume();
    }
}