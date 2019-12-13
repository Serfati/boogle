package Model;

import Model.Engine.InvertedIndex;
import javafx.collections.ObservableList;

import java.io.File;
import java.util.*;

/**
 * This class is from MVVM architecture.
 */
public class MyModel extends Observable implements IModel {
    public static InvertedIndex invertedIndex;
    public static HashSet<String> stopWords;
    public HashMap<String, LinkedList<String>> m_results;


    @Override
    public void startIndexing(String pathOfDocs, String destinationPath, boolean stm) {
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
        boolean foundInvertedIndex = false;
        File dirSource = new File(path);
        File[] directoryListing = dirSource.listFiles();
        String[] update = new String[0];
        if (directoryListing != null && dirSource.isDirectory()) {
            for(File file : directoryListing) { // search for the relevant file
                if ((file.getName().equals("StemInvertedFile.txt") && stem) || (file.getName().equals("InvertedFile.txt")) && !stem) {
                    invertedIndex = new InvertedIndex(file);
                    foundInvertedIndex = true;
                }
            }
            invertedIndex = null;
            update = new String[]{"Fail", "could not find one or more dictionaries"};
        } else
            update = new String[]{"Fail", "destination path is illegal or unreachable"};

        setChanged();
        notifyObservers(update);
    }

    /**
     * this function checks if all the paths are valid
     *
     * @param pathOfDocs      - path of the corpus and stop words
     * @param destinationPath - path where the postings and other data should be written
     * @return returns true if all the paths are valid or raises a flag that something is wrong
     */
    private String[] pathsAreValid(String pathOfDocs, String destinationPath) {
        String pathOfStopWords = "", corpusPath = pathOfDocs;
        File dirSource = new File(pathOfDocs);
        File[] directoryListing = dirSource.listFiles();
        if (directoryListing != null && dirSource.isDirectory()) {
            for(File file : directoryListing) {
                if (file.isFile() && file.getName().equalsIgnoreCase("stop_words.txt"))
                    pathOfStopWords = file.getAbsolutePath();
            }
            if (pathOfStopWords.equals("")) {
                String[] update = {"Fail", "contents of source path do not contain corpus folder or stop words file"};
                setChanged();
                notifyObservers(update);
                return null;
            }
        } else {
            String[] update = {"Fail", "Source path is illegal or unreachable"};
            setChanged();
            notifyObservers(update);
            return null;
        }
        File dirDest = new File(destinationPath);
        if (!dirDest.isDirectory()) {
            String[] update = {"Fail", "Destination path is illegal or unreachable"};
            setChanged();
            notifyObservers(update);
            return null;
        }
        return new String[]{corpusPath, destinationPath};
    }

    /**
     * Delete the postings file
     *
     * @param postingsPath
     */
    public void startOver(String postingsPath) {
        File resourceDirectory = new File(postingsPath);
        File[] allDirectory = resourceDirectory.listFiles();
        assert allDirectory != null;
        Arrays.stream(allDirectory).forEachOrdered(f -> {
            try {
                f.delete();
            } catch(Exception ignored) {
            }
        });
    }
}
