package Model;

import Controller.Controller;
import Engine.Indexer;
import IO.ReadFile;
import Parser.Parse;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;


public class Model {
    public static HashSet<String> stopWordSet;
    private static Logger logger = LogManager.getLogger(Model.class);
    private ReadFile rf;
    private Indexer indexer;
    private Parse parser;
    private Controller controller;

    public Model(ReadFile rf, Indexer indexer, Parse parser) {
        this.rf = rf;
        this.indexer = indexer;
        this.parser = parser;
        logger.info("ctor of myModel");
    }

    public Model() {
    }

    public void setController(Controller controller) {
        this.controller = controller;
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
