package View;

import javafx.fxml.Initializable;
import javafx.scene.control.Label;
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
    public Label newWindowLabel2;


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

        extraLabel.setText("\nHere are all the extras we added to the engine:\n\n");

        newWindowLabel.setText("1. Check the import list at the bottom of this file before running the executable file.\n"+
                "\t* The folder \"import files.zip\" contains some of the imports for the project.\n"+
                "\n"+
                "2. Check that at least 'Corpus size' is available in the Posting's path on the disk.\n"+
                "\t\t\t\n"+
                "* Executable is located in the project's folder.\n"+
                "\n"+
                "How to use the engine:\n"+
                "\t- in case you want to build the engine on a new corpus:\n"+
                "\t\t- At part 1 choose the directory of the new corpus at 'Corpus path'.\n"+
                "\t\t- Choose a path for the files to be saved in at 'Posting path'.\n"+
                "\t\t- Press build and wait untill the engine finishes.\n"+
                "\t\t  Meanwhile you can check our cool & smart progress bar that works according to the actual corpus and merging process.\n"+
                "\t\t- After the build is done click on 'Upload' to load the data.\n"+
                "\t\t- Now you can switch to Part 2 by clicking it at the top right corner.\n"+
                "\t\t- For understading Part 2 please read the instructions for it below.\n"+
                "\t-in case you already built the engine before and just want the search the data:\n"+
                "\t\t- Choose the path of the posting files saved with 'Posting path'.\n"+
                "\t\t- Click on 'Upload' to upload the data.\n"+
                "\t\t- Now you can switch to Part 2 by clicking it at the top right corner.\n"+
                "\t\t- At Part 2 you can either enter your own query by simply writing it at 'Query' and click 'Run query'\n"+
                "\t\t  or you can enter a file through the 'Query File' and click 'Run query from file'.\n"+
                "\t\t- You can choose to use Stem and Sementics by checking the check-box\n"+
                "\t\t- After the run you can save the output to a file. make sure you chose a directory path at 'Save Results'\n"+
                "\t\t- You can see the most dominant Entities of the files that came back from the query by clicking 'Show Entities'\t");

        MenuBar.setText("\n"+
                "File - \n"+
                "   Load corpus: Opens up another window where you can specify your corpus folder.\n"+
                "   Go to search tab: for part B of engine.\n"+
                "   Save: You can either save this dic .\n"+
                "   Load: Choose a dic from the previous saved dics.\n"+
                "\nOptions -\n"+
                "\nExit -\n"+
                "   Closes the application by clicking the *Leave* button\n"+
                "\nAbout -\n    Read about this Search engine and it's creators.\n\n"+
                "The Status Bar will help you understand what's going on in the software.");

        soundLabel.setText("");

        newLabel.setText("");

        getHintLabel.setText("");

        solveLabel.setText("");

        initImages();
    }


    private void initImages() {

        File file = new File("src/main/resources/searchGif.gif");
        Image image = new Image(file.toURI().toString());
        soundImage.setImage(image);

        file = new File("src/main/resources/icon_new.png");
        image = new Image(file.toURI().toString());
        getHintImage.setImage(image);

        file = new File("src/main/resources/SaveResult.png");
        image = new Image(file.toURI().toString());
        solveImage.setImage(image);

        file = new File("src/main/resources/end.png");
        image = new Image(file.toURI().toString());
        newSearchImage.setImage(image);
    }

    public void close() {
        Stage s = (Stage) OK.getScene().getWindow();
        s.close();
    }
}