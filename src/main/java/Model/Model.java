package Model;

import Model.Engine.DocDictionaryNode;
import Model.Engine.InvertedIndex;
import Model.IO.ReadFile;
import Model.IO.WriteFile;
import javafx.collections.ObservableList;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Observable;

/**
 * This class is from MVVM architecture.
 */

public class Model extends Observable implements IModel {
    public static InvertedIndex invertedIndex;
    public static HashMap<String, DocDictionaryNode> documentDictionary;
    public static HashSet<String> stopWords;
    private boolean dictionaryIsStemmed = false;

    @Override
    public void startIndexing(String pathOfDocs, String destinationPath, boolean stm) {
        String[] paths = pathsAreValid(pathOfDocs, destinationPath);
        if (paths != null) {
            double start = System.currentTimeMillis();
            Manager man = new Manager();
            dictionaryIsStemmed = stm;
            stopWords = ReadFile.initSet(pathOfDocs+"/stop_words.txt");
            invertedIndex = new InvertedIndex();
            documentDictionary = new HashMap<>();
            double[] results = new double[2];
            try {
                results = man.manage(invertedIndex, documentDictionary, paths[0], paths[1], stm);
                writeDictionariesToDisk(destinationPath, stm);
            } catch(Exception e) {
                String[] update = {"Fail", "Indexing failed"};
                setChanged();
                notifyObservers(update);
            }
            double[] totalResults = new double[]{results[0], results[1], (System.currentTimeMillis()-start) / 60000};
            setChanged();
            notifyObservers(totalResults);
        }
    }

    public void loadDictionary(String path, boolean stem) {
        File dirSource = new File(path);
        File[] directoryListing = dirSource.listFiles();
        String[] update;
        if (directoryListing != null && dirSource.isDirectory()) {
            // search for the relevant file
            Arrays.stream(directoryListing).filter(file -> (file.getName().equals("StemInvertedFile.txt") && stem) || (file.getName().equals("InvertedFile.txt")) && !stem).forEachOrdered(file -> invertedIndex = new InvertedIndex(file));
            invertedIndex = null;
            update = new String[]{"Fail", "could not find one or more dictionaries"};
        } else
            update = new String[]{"Fail", "destination path is illegal or unreachable"};

        setChanged();
        notifyObservers(update);
    }


    private String[] pathsAreValid(String pathOfDocs, String destinationPath) {
        String pathOfStopWords = "";
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
        return new String[]{pathOfDocs, destinationPath};
    }


    @Override
    public void startOver(String path) {
        File dir = new File(path);
        String[] update;
        if (dir.isDirectory()) try {
            FileUtils.cleanDirectory(dir);//delete all the files in the directory
            update = new String[]{"Successful", "The folder is clean now"};
        } catch(IOException e) {
            e.printStackTrace();
            update = new String[]{"Fail", "Cleaning the folder was unsuccessful"};
        }
        else update = new String[]{"Fail", "Path given is not a directory or could not be reached"};
        setChanged();
        notifyObservers(update);
    }

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

    private void writeDictionariesToDisk(String destinationPath, boolean stem) {
        Thread tInvertedFile = new Thread(() -> WriteFile.writeInvertedFile(destinationPath, invertedIndex, stem));
        tInvertedFile.start();
        Thread tDocs = new Thread(() -> WriteFile.writeDocDictionary(destinationPath, documentDictionary, stem));
        tDocs.start();
        try {
            tInvertedFile.join();
            tDocs.join();
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }
}

