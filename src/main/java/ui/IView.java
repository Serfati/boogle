package ui;

import javafx.event.ActionEvent;
import view_model.ViewModel;

import java.util.Observable;
import java.util.Observer;

public interface IView extends Observer {

    /**
     * @param viewModel the view model of the MVVM */
    void setViewModel(ViewModel viewModel);

    /* Run the process - parse and index */
    void onStartClick();

    /* Browse click to find corpus and stop words path*/
    void browseCorpusClick(ActionEvent actionEvent);

    /* Browse click to find output folder path*/
    void browseOutputClick(ActionEvent actionEvent);

    /* Browse click to find output folder path*/
    void browseStopwordsClick(ActionEvent actionEvent);

    /* Load dictionary request*/
    void loadDictionary(ActionEvent event);

    /* Save dictionary request*/
    void saveDictionary(ActionEvent event);

    @Override
    void update(Observable o, Object arg);
}