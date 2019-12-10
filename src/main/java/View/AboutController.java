package View;

import javafx.fxml.Initializable;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class AboutController implements Initializable {
    public javafx.scene.control.Button close;
    public javafx.scene.control.Label Itext;

    public void close() {
        Stage s = (Stage) close.getScene().getWindow();
        s.close();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Itext.setWrapText(true);
        Itext.setText("Copyright 2020 Avihai Serfati and Yarden Levy\n"+
                "\n"+
                "Permission is hereby granted, free of charge, to any "+"\n"+
                "person obtaining a copy of this software and associated "+"\n"+
                "documentation files (the \"Software\"), to deal in the "+"\n"+
                "Software without restriction, including without limitation "+"\n"+
                "the rights to use, copy, modify, merge, publish, distribute, "+"\n"+
                "sublicense, and/or sell copies of the Software, and to permit "+"\n"+
                "persons to whom the Software is furnished to do so, subject "+"\n"+
                "to the following conditions:\n"+
                "The above copyright notice and this permission notice shall be "+"\n"+
                "included in all copies or substantial portions of the Software.\n"+
                "\n"+
                "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, "+"\n"+
                "EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF "+"\n"+
                "MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. "+"\n"+
                "IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, "+"\n"+
                "DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, "+"\n"+
                "ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE "+"\n"+
                "OR OTHER DEALINGS IN THE SOFTWARE.");
    }
}