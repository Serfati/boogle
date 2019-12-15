package ViewModel;

import Model.IModel;
import javafx.application.Platform;

import java.io.File;
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
     * transfers to the model the contents of the start indexing click
     *
     * @param pathOfDocs      - path of the corpus
     * @param destinationPath - path where the posting files and other files will be written
     * @param stopWordsPath   - path of the stop words file
     * @param stm             - if this indexing includes stemming or not
     */
    public void onStartClick(String pathOfDocs, String stopWordsPath, String destinationPath, boolean stm) {
        Platform.runLater(() -> model.startIndexing(pathOfDocs, stopWordsPath, destinationPath, stm));
    }

    /**
     * transfers to the model a delete of all contents request in the path given
     *
     * @param path the path that all his contents will be deleted
     */
    public void onStartOverClick(String path) {
        Platform.runLater(() -> model.startOver(path));
    }

    /**
     * transfers to the model a show dictionary request
     */
    public void showDictionary() {
        Platform.runLater(() -> model.showDictionary());
    }

    /**
     * transfers to the model a request to load a dictionary from a specific file
     *
     * @param path path of the file
     * @param stem load with stemming or without
     */
    public void loadDictionary(String path, boolean stem) {
        Platform.runLater(() -> model.loadDictionary(path, stem));
    }

    public void saveDictionary(File file) {
    }
}
