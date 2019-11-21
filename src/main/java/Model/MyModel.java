package Model;


import Model.Engine.Indexer;
import Model.IO.ReadFile;
import Model.Parser.Parse;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MyModel extends Observable implements IModel {
    public static HashSet<String> stopWordSet;
    private static Logger logger = LogManager.getLogger(MyModel.class);
    private ReadFile rf;
    private Indexer indexer;
    private Parse parser;
    private ExecutorService threadPool = Executors.newCachedThreadPool();
    private boolean isFinished;

    public MyModel(ReadFile rf, Indexer indexer, Parse parser) {
        this.rf = rf;
        this.indexer = indexer;
        logger.info("ctor of myModel");
    }

    //ctor
    public MyModel() {
        isFinished = false;
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

    public void loadStopWordsList(String pathOfStopWords) throws IOException {

        File f = new File(pathOfStopWords);
        StringBuilder allText = new StringBuilder();
        FileReader fileReader = new FileReader(f);
        try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            String line;
            while((line = bufferedReader.readLine()) != null)
                allText.append(line).append("\n");
            String[] stopWords = allText.toString().split("\n");
            Collections.addAll(stopWordSet, stopWords);
        } catch(IOException e) {
            logger.error("stopwords file not found in the specified path. running without stopwords");
        }
    }
}
