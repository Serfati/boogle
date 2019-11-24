package ViewModel;

import Model.IModel;
import javafx.application.Platform;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

public class MyViewModel extends Observable implements Observer {
    private IModel model;

    /**
     * constructs a view model by holding a model
     *
     * @param model the model of the MVVM
     */
    public MyViewModel(IModel model) {
        this.model = model;
    }

    /**
     * this function is called when the model raises a flag that something has changed
     * @param o - who changed
     * @param arg - the change
     */
    public void update(Observable o, Object arg) {
        if (o == model){
            setChanged();
            notifyObservers(arg);
        }
    }

    /* transfers to the model a show dictionary request*/
    public void showDictionary() {
        Platform.runLater(() -> model.showDictionary());
    }

    public void loadDictionary(File file, boolean stem) {
        Platform.runLater(() -> model.loadDictionary(file, stem));
    }

    public void saveDictionary(File file) {
        model.saveDic(file);
    }

    public void closeModel() {
        model.closeModel();
    }

    public boolean isFinish() {
        return false;
    }

    public void onStartClick(String pathOfDocs, String destinationPath, boolean stm) {
        Platform.runLater(() -> model.startIndexing(pathOfDocs, destinationPath,stm));
    }

}