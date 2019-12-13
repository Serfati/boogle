package View;

import ViewModel.ViewModel;
import javafx.event.ActionEvent;

import java.util.Observable;
import java.util.Observer;

public interface IView extends Observer {

    /**
     * constructor of view, connect the view to the viewModel
     *
     * @param viewModel the view model of the MVVM
     */
    void setViewModel(ViewModel viewModel);

    /* This function starts the process of parse and index the dictionary*/
    void onStartClick();

    /* This function lets the user select his corpus and stop words path*/
    void browseCorpusClick(ActionEvent actionEvent);

    /* This function lets the user select his  location to save the postings and other dat*/
    void browseOutputClick(ActionEvent actionEvent);

    /* This function lets the user select his stop words path*/
    void browseStopwordsClick(ActionEvent actionEvent);

    /* transfers to the view model a load dictionary request*/
    void loadDictionary(ActionEvent event);

    /* transfers to the view model a save dictionary request*/
    void saveDictionary(ActionEvent event);

    @Override
    void update(Observable o, Object arg);
}