package View;

import javafx.scene.control.Alert;

class MyAlert {

    static void showAlert(String text) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(Alert.AlertType.ERROR);
        alert.setContentText(text);
        alert.show();
    }
}
