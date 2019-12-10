package View;

import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class HelpController implements Initializable {
    public javafx.scene.control.Label newWindowLabel;
    //public javafx.scene.control.Label newWindowLabe2;
    public javafx.scene.control.Label MenuBar;
    public javafx.scene.control.Button OK;
    public javafx.scene.image.ImageView soundImage;
    public javafx.scene.image.ImageView soundImage1;
    public javafx.scene.image.ImageView newSearchImage;
    public javafx.scene.image.ImageView getHintImage;
    public javafx.scene.image.ImageView solveImage;
    public javafx.scene.image.ImageView resetZoomImage;
    public javafx.scene.control.Label soundLabel;
    public javafx.scene.control.Label newLabel;
    public javafx.scene.control.Label getHintLabel;
    public javafx.scene.control.Label solveLabel;
    public javafx.scene.control.Label extraLabel;
    public javafx.scene.control.Label resetZoomLabel;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        newWindowLabel.setWrapText(true); // lets the label text to break row when it is in need.
        MenuBar.setWrapText(true);
        soundLabel.setWrapText(true);
        newLabel.setWrapText(true);
        getHintLabel.setWrapText(true);
        solveLabel.setWrapText(true);
        extraLabel.setWrapText(true);
        resetZoomLabel.setWrapText(true);

        extraLabel.setText("");

        newWindowLabel.setText("");

        MenuBar.setText("");

        soundLabel.setText("");

        newLabel.setText("");

        getHintLabel.setText("");

        solveLabel.setText("");

        initImages();
    }


    private void initImages() {

        File file = new File("resources/searchGif.gif");
        Image image = new Image(file.toURI().toString());
        soundImage.setImage(image);

        file = new File("resources/searchGif.gif");
        image = new Image(file.toURI().toString());
        getHintImage.setImage(image);

        file = new File("resources/searchGif.gif");
        image = new Image(file.toURI().toString());
        solveImage.setImage(image);

        file = new File("resources/icon_new.png");
        image = new Image(file.toURI().toString());
        newSearchImage.setImage(image);
    }

    public void close() {
        Stage s = (Stage) OK.getScene().getWindow();
        s.close();
    }
}