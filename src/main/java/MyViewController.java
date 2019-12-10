import Model.Structures.ShowDictionaryRecord;
import View.MyAlert;
import ViewModel.MyViewModel;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.ResourceBundle;

public class MyViewController implements Observer, Initializable {

    @FXML
    public ImageView icon_3;
    public ImageView boogle_logo;
    public ImageView icon_4;
    public ImageView icon_2;
    public ImageView icon5;
    @FXML
    public Label lbl_statusBar;
    public TableView<ShowDictionaryRecord> table_showDic;
    public MenuItem save_MenuItem;
    public MenuItem load_MenuItem;
    public TableColumn<ShowDictionaryRecord, String> tableCol_term;
    public TableColumn<ShowDictionaryRecord, Number> tableCol_count;
    public TextField txtfld_output_location;

    @FXML
    public TextField txtfld_corpus_location;
    public TextField txtfld_stopwords_location;
    public CheckBox checkbox_memory_saver;
    @FXML
    public Button btn_corpus_browse;
    public Button btn_show_dictionary;
    public Button btn_generate_index;
    public Button btn_stopwords_browse;
    public Button btn_output_browse;
    public Button btn_reset;
    public Button btn_load_dictionary;
    private MyViewModel myViewModel;
    @FXML
    private CheckBox checkbox_use_stemming;


    /**
     * constructor of view, connect the view to the viewModel
     *
     * @param myViewModel the view model of the MVVM
     */
    public void setViewModel(MyViewModel myViewModel) {
        this.myViewModel = myViewModel;
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initImages();
//        Tooltip tooltip = new Tooltip();
//        tooltip.setText("");
//        checkbox_memory_saver.setTooltip(tooltip);
    }

    private void initImages() {
        File file = new File("src/main/resources/start.png");
        Image image = new Image(file.toURI().toString());
        icon_4.setImage(image);
        setClick(icon_4);

        file = new File("src/main/resources/SaveResult.png");
        image = new Image(file.toURI().toString());
        icon_3.setImage(image);
        setClick(icon_3);

        file = new File("src/main/resources/icon_new.png");
        image = new Image(file.toURI().toString());
        icon5.setImage(image);
        setClick(icon5);

        file = new File("src/main/resources/searchGif.gif");
        image = new Image(file.toURI().toString());
        icon_2.setImage(image);
        setClick(icon_2);

        file = new File("src/main/resources/logo.png");
        image = new Image(file.toURI().toString());
        boogle_logo.setImage(image);

    }

    /**
     * This function starts the process of parse and index the dictionary
     */
    public void onStartClick() {
        if (txtfld_corpus_location.getText().equals("") || txtfld_output_location.getText().equals(""))// check if the paths are not empty
            MyAlert.showAlert("path can not be empty");
        else
            myViewModel.onStartClick(txtfld_corpus_location.getText(), txtfld_output_location.getText(), checkbox_use_stemming.isSelected()); //transfer to the view Model
    }

    private void setClick(javafx.scene.image.ImageView icon) {
        icon.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            help();
            event.consume();
        });
    }

    /**
     * transfers a request to show the dictionary of the current indexing
     */
    public void showDictionaryClick() {
        myViewModel.showDictionary();
    }

    /**
     * shows an observable list that contains all the data about the current indexing: Term and TF
     *
     * @param records all the data about the current indexing
     */
    private void showDictionaryClick(ObservableList<ShowDictionaryRecord> records) {
        if (records != null) {
            tableCol_term.setCellValueFactory(cellData -> cellData.getValue().getTermProperty());
            tableCol_count.setCellValueFactory(cellData -> cellData.getValue().getCountProperty());
            table_showDic.setItems(records);
        }
        btn_show_dictionary.setDisable(false);
    }

    public boolean isUseStemming() {
        return checkbox_use_stemming.isSelected();
    }

    public void generateIndex(ActionEvent actionEvent) {

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
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Output Location");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        File corpusDir = directoryChooser.showDialog(new Stage());
        if (null != corpusDir)  //directory chosen
            txtfld_output_location.setText(corpusDir.getAbsolutePath());
    }

    public void browseStopwordsClick(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Stopwords file Location");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        File corpusDir = fileChooser.showOpenDialog(new Stage());
        if (null != corpusDir)  //directory chosen
            txtfld_stopwords_location.setText(corpusDir.getAbsolutePath());
    }

    private void handleNewDictionary(Alert result) {
        if (result.getAlertType() == Alert.AlertType.ERROR)
            result.showAndWait();
        else {
            result.show();
            btn_reset.setDisable(false);
            btn_show_dictionary.setDisable(false);
        }
    }

    public void exitButton() {
        exitCorrectly();
    }

    public void exitCorrectly() {
        Alert alert = new Alert(Alert.AlertType.NONE);
        ButtonType leaveButton = new ButtonType("Leave", ButtonBar.ButtonData.YES);
        ButtonType stayButton = new ButtonType("Stay", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(stayButton, leaveButton);
        alert.setContentText("Are you sure you want to exit??");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == leaveButton)
            Platform.exit();
        else
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
        Scene scene = new Scene(root, 450, 400);
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

    public void reset(ActionEvent actionEvent) {
        btn_show_dictionary.setDisable(true);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o != myViewModel) {
            return;
        }
        if (arg != null) {
            String argument = (String) arg;

        }
    }

    public void saveDictionary(ActionEvent event) {
        int[] choose = {0};
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Save Dictionary");
        alert.setContentText("Which Dictionary do you want to save?");
        ButtonType okButton = new ButtonType("Current", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("Original", ButtonBar.ButtonData.NO);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(okButton, noButton, cancelButton);
        alert.showAndWait().ifPresent(type -> {
            if (type == okButton) //Current
                choose[0] = 1;
            else if (type == noButton)  //Original
                choose[0] = 2;
        });
        if (choose[0] == 0) {
            lbl_statusBar.setText("Save was canceled");
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

        if (file != null) {
            if (choose[0] == 1) {
                myViewModel.saveDictionary(file); //TODO need special method
                lbl_statusBar.setText("Current dictionary saved");
            } else {
                myViewModel.saveDictionary(file);
                lbl_statusBar.setText("Original dictionary saved");
            }
        }
        event.consume();
    }

    /**
     * transfers to the view model a load dictionary request
     */
    public void loadDictionary(ActionEvent event) {
        System.out.println("loadFile");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a dictionary to load");
        File filePath = new File("./dictionaries/");
        if (!filePath.exists())
            filePath.mkdir();
        fileChooser.setInitialDirectory(filePath);

        File file = fileChooser.showOpenDialog(new PopupWindow() {
        });
        if (file != null && file.exists() && !file.isDirectory()) {
            myViewModel.loadDictionary(file.getAbsolutePath(), checkbox_use_stemming.isSelected());
            lbl_statusBar.setText("Loaded "+file.getName());

        } else
            MyAlert.showAlert("Please choose a vaild destination");
        event.consume();
    }

    public void onAction_Property() {
        try {
            Stage stage = new Stage();
            stage.setTitle("Properties");
            FXMLLoader fxmlLoader = new FXMLLoader();
            Parent root = fxmlLoader.load(getClass().getResource("MyPropertiesView.fxml").openStream());
            Scene scene = new Scene(root, 400, 370);
            scene.getStylesheets().add(getClass().getResource("ViewStyle.css").toExternalForm());
            stage.setScene(scene);
//            PropertiesViewController propertiesViewController = fxmlLoader.getController();
//            propertiesViewController.setStage(stage);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch(Exception ignored) {
        }
    }

}
