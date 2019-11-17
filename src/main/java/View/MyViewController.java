package View;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.stage.Stage;

import java.util.Optional;

public class MyViewController {
    public Label lbl_characterRow;
    public Label lbl_characterColumn;
    public Label lbl_statusBar;
    public Label label_mainCharacterRow;
    public Label label_mainCharacterCol;
    public MenuItem save_MenuItem;
    public MenuItem solve_MenuItem;

    @FXML
    public javafx.scene.image.ImageView icon_sound;
    public javafx.scene.image.ImageView icon_partSolution;
    public javafx.scene.image.ImageView icon_fullSolution;
    public javafx.scene.image.ImageView icon_makeNewMaze;
    public javafx.scene.image.ImageView icon_zoomImageView;
    public javafx.scene.control.ScrollPane ScrollPane;
    public ScrollPane scrollPane;
    private String soundOnOff;
    private Stage stageNewGameController;

    public void scrollInOut(ScrollEvent scrollEvent) {
    }

    public void KeyPressed(KeyEvent keyEvent) {

    }

    public void exitButton() {
        exitCorrectly();
    }

    private void exitCorrectly() {
        Alert alert = new Alert(Alert.AlertType.NONE);
        ButtonType leaveButton = new ButtonType("Leave", ButtonBar.ButtonData.YES);
        ButtonType stayButton = new ButtonType("Stay", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(stayButton, leaveButton);
        alert.setContentText("Are you sure you want to exit??");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == leaveButton) {
            // ... user chose to Leave
            // Close program
            //myViewModel.closeModel();
            Platform.exit();
        } else
            // ... user chose to CANCEL or closed the dialog
            alert.close();
    }
}
