package Model;

import Model.Engine.InvertedIndex;
import javafx.collections.ObservableList;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Observable;

/**
 * This class is from MVVM architecture.
 */
public class MyModel extends Observable implements IModel {
    public static InvertedIndex invertedIndex;

    public static HashSet<String> languages;
    public static HashSet<String> stopWords;
    public static HashSet<String> usedCities;
    public static HashSet<String> usedLanguages;
    public HashMap<String, LinkedList<String>> m_results;
    private boolean dictionaryIsStemmed = false;


    @Override
    public void saveDic(File file) {

    }

    @Override
    public void startIndexing(String pathOfDocs, String destinationPath, boolean stm) {

    }

    @Override
    public void closeModel() {
        System.out.println("Close Model");
    }

    @Override
    public boolean isFinish() {
        return false;
    }


    /**
     * loads the inverted index and notifies the view model
     */
    @Override
    public void showDictionary() {
        if (invertedIndex == null) {
            String[] update = {"Fail", "Please load the dictionary first"};
            setChanged();
            notifyObservers(update);
        } else {
            ObservableList records = invertedIndex.getRecords();
            setChanged();
            notifyObservers(records);
        }
    }


    /**
     * loads the dictionary that is present in the path given according to the stem sign
     *
     * @param path - the path to load from
     * @param stem - dictionary with stem or without
     */

    public void loadDictionary(String path, boolean stem) {
        boolean foundInvertedIndex = false, foundDocumentDictionary = false, foundCityDictionary = false, foundLanguages = false;
        File dirSource = new File(path);
        File[] directoryListing = dirSource.listFiles();
        String[] update = new String[0];
        if (directoryListing != null && dirSource.isDirectory()) {
            for(File file : directoryListing) { // search for the relevant file
                if ((file.getName().equals("StemInvertedFile.txt") && stem) || (file.getName().equals("InvertedFile.txt")) && !stem) {
                    dictionaryIsStemmed = stem;
                    invertedIndex = new InvertedIndex(file);
                    foundInvertedIndex = true;
                }
            }
            if (!foundInvertedIndex || !foundDocumentDictionary || !foundCityDictionary || !foundLanguages) {
                invertedIndex = null;
                update = new String[]{"Fail", "could not find one or more dictionaries"};
            } else
                update = new String[]{"Successful", "Dictionary was loaded successfully"};
        } else
            update = new String[]{"Fail", "destination path is illegal or unreachable"};

        setChanged();
        notifyObservers(update);
    }
}
