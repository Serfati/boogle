package View;

import javafx.scene.control.Alert;

class MyAlert {

    static void showAlert(Alert.AlertType at, String text) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(at);
        alert.setContentText(text);
        alert.show();
    }
}
