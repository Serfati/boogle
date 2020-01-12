package view_model;

import javafx.application.Platform;
import model.IModel;

import java.util.Observable;
import java.util.Observer;

public class ViewModel extends Observable implements Observer {
    private IModel model;

    /**
     * constructs a view model by holding a model
     *
     * @param model the model of the MVVM
     */
    public ViewModel(IModel model) {
        this.model = model;
    }

    public void onStartClick(String pathOfDocs, String destinationPath, boolean stm) {
        Platform.runLater(() -> model.startIndexing(pathOfDocs, destinationPath, stm));
    }

    public void onStartOverClick(String path) {
        Platform.runLater(() -> model.reset(path));
    }

    /**
     * this function is called when the model raises a flag that something has changed
     *
     * @param o   - who changed
     * @param arg - the change
     */
    public void update(Observable o, Object arg) {
        if (o == model) {
            setChanged();
            notifyObservers(arg);
        }
    }

    /**
     * transfers to the model a show dictionary request
     */
    public void showDictionary() {
        Platform.runLater(() -> model.showDictionary());
    }

    /**
     * transfers to the model a show results request
     */
    public void showData() {
        Platform.runLater(() -> model.showData());
    }

    public void loadDictionary(String path, boolean stem) {
        Platform.runLater(() -> model.loadDictionary(path, stem));
    }

    public void onSearchBoogleClick(String posting, String query, String outLocation, boolean stem, boolean semantic, boolean offline) {
        Platform.runLater(() -> model.startBoogleSearch(posting, query, outLocation, stem, semantic, offline));
    }

    public StringBuilder showFiveEntities(String docName) {
        return model.showFiveEntities(docName);
    }

    public boolean writeRes(String dest) {
        return model.writeRes(dest);
    }
}
