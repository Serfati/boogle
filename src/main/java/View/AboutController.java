package View;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

public class AboutController implements Initializable {

    private static final String LINKED_IN = "https://www.linkedin.com/";
    private static final String FACEBOOK = "http://facebook.com/";
    private static final String WEBSITE = "http://www.ynet.com";
    private static final String YOUTUBE = "https://www.youtube.com/";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        AlertMaker.showSimpleAlert("Author", "Hello ! Thanks for trying out Boogle search engine");
    }

    private void loadWebpage(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch(IOException | URISyntaxException e1) {
            e1.printStackTrace();
            handleWebpageLoadException(url);
        }
    }

    private void handleWebpageLoadException(String url) {
        WebView browser = new WebView();
        WebEngine webEngine = browser.getEngine();
        webEngine.load(url);
        Stage stage = new Stage();
        Scene scene = new Scene(new StackPane(browser));
        stage.setScene(scene);
        stage.setTitle("Genuine Coder");
        stage.show();
    }

    @FXML
    private void loadYoutubeChannel(ActionEvent event) {
        loadWebpage(YOUTUBE);
    }

    @FXML
    private void loadBlog(ActionEvent event) {
        loadWebpage(WEBSITE);
    }

    @FXML
    private void loadLinkedIN(ActionEvent event) {
        loadWebpage(LINKED_IN);
    }

    @FXML
    private void loadFacebook(ActionEvent event) {
        loadWebpage(FACEBOOK);
    }
}
