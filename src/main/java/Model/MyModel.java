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


public class MyModel extends Observable implements IModel {
    public static HashSet<String> stopWordSet;
    private final Logger logger = Logger.getLogger(MyModel.class);
    public static Parse myDocumentsParser;
    private static MyModel singleton = null;
    private ReadFile myFileReader;
    private Indexer myIndexer;
    private Boolean useStemming;
    private Boolean useSemantic;
    private ExecutorService threadPool = Executors.newCachedThreadPool();
    private boolean isFinished;

    //constructor
    public MyModel() {
//        myIndexer=new Indexer();
//        myDocumentsParser = new Parse();
//        myFileReader=new ReadFile();
        useStemming = false;
        useSemantic = false;
    }

    public static MyModel getInstance() {
        if (singleton == null)
            singleton = new MyModel();
        return singleton;

    }


    @Override
    public void loadDic(File file) {

    }

    @Override
    public void saveDic(File file) {

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

}
