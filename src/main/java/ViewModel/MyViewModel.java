package ViewModel;

import Model.IModel;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

public class MyViewModel extends Observable implements Observer {
    private IModel model;
    private boolean isRunning = true;

    public MyViewModel(IModel model) {
        this.model = model;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o == model) {
            if (arg != null) {
                String argument = (String) arg;
            }
            setChanged();
            notifyObservers(arg);
        }
    }

    public void closeModel() {
        model.closeModel();
    }

    public boolean isFinish() {
        return model.isFinish();
    }

    public void loadDic(File file) {
        model.loadDic(file);
    }

    public void saveDic(File file) {
        model.saveDic(file);
    }

}