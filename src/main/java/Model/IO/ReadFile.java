package Model.IO;

import Model.Parser.cDocument;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class has the responsibility to get the corpus and splite the file into cDocuments.
 */
public class ReadFile {

    public static LinkedList<cDocument> readFiles(String pathOfDocs, int iterator, int total) {
        File dir = new File(pathOfDocs);
        File[] directoryListing = dir.listFiles();
        LinkedList<cDocument> allDocsInCorpus = new LinkedList<>();
        if (directoryListing != null && dir.isDirectory()) {
            int start = iterator * directoryListing.length / total;
            int end = ((iterator+1) * directoryListing.length / total)-1;
            ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
            LinkedList<Future<LinkedList<cDocument>>> futureDocsInFile = IntStream.rangeClosed(start, end).mapToObj(i -> pool.submit(new Reader(directoryListing[i]))).collect(Collectors.toCollection(LinkedList::new));
            futureDocsInFile.forEach(f -> {
                try {
                    allDocsInCorpus.addAll(f.get());
                } catch(InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            });
            pool.shutdown();
        }
        return allDocsInCorpus;
    }

    public static HashSet<String> initSet(String fileName) {
        HashSet<String> set = new HashSet<>();
        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            set = bufferedReader.lines().collect(Collectors.toCollection(HashSet::new));
            bufferedReader.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
        return set;
    }
}


