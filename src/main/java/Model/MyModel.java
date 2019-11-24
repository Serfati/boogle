package Model;


import Model.Engine.Indexer;
import Model.IO.ReadFile;
import Model.Parser.Parse;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.HashSet;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class is from MVVM architecture.
 */
public class MyModel extends Observable implements IModel {
    public static HashSet<String> stopWordSet;
    private final Logger logger = Logger.getLogger(MyModel.class);
    public static Parse myDocumentsParser;
    private ReadFile myFileReader;
    private Indexer myIndexer;
    private Boolean useStemming;
    private ExecutorService threadPool = Executors.newCachedThreadPool();
    private boolean isFinished;

    //constructor
    public MyModel() {
//        myIndexer=new Indexer();
//        myDocumentsParser = new Parse();
//        myFileReader=new ReadFile();
        useStemming = false;
    }

    @Override
    public void saveDic(File file) {

    }

    @Override
    public void startIndexing(String pathOfDocs, String destinationPath, boolean stm) {

    }

    @Override
    public void closeModel() {
        threadPool.shutdown();
        System.out.println("Close Model");
    }

    @Override
    public boolean isFinish() {
        return isFinished;
    }

    @Override
    public void showDictionary() {

    }

    @Override
    public void loadDictionary(File file, boolean stem) {


    }

}
