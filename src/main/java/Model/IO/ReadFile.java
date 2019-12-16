package Model.IO;

import Model.Parser.cDocument;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * This class has the responsibility to get the corpus and splite the file into cDocuments.
 */
public class ReadFile {

    public static LinkedList<cDocument> readFiles(String pathOfDocs, int mone, int mechane) {
        File dir = new File(pathOfDocs);
        File[] directoryListing = dir.listFiles();
        LinkedList<cDocument> allDocsInCorpus = new LinkedList<>();
        if (directoryListing != null && dir.isDirectory()) {
            int start = mone * directoryListing.length / mechane;
            int end = ((mone+1) * directoryListing.length / mechane)-1;
            ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
            LinkedList<Future<LinkedList<cDocument>>> futureDocsInFile = new LinkedList<>();
            //go throw end-start FILES
            for(int i = start; i <= end; i++) {
                futureDocsInFile.add(pool.submit(new Reader(directoryListing[i])));
            }
            //add together all the lists of the corpus docs to one list
            for(Future<LinkedList<cDocument>> f : futureDocsInFile) {
                try {
                    allDocsInCorpus.addAll(f.get());
                } catch(InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
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

    static class Reader implements Callable<LinkedList<cDocument>> {
        File file;

        Reader(File file) {
            this.file = file;
        }

        @Override
        public LinkedList<cDocument> call() {
            File[] directoryListing = file.listFiles();
            LinkedList<cDocument> fileList = new LinkedList<>();
            if (directoryListing != null && file.isDirectory())
                Arrays.stream(directoryListing).map(this::read).forEach(fileList::addAll);
            return fileList;
        }

        private LinkedList<cDocument> read(File fileToSeparate) {
            LinkedList<cDocument> docList = new LinkedList<>();
            try {
                Document document = Jsoup.parse(new String(Files.readAllBytes(Objects.requireNonNull(file.listFiles())[0].toPath())));
                assert document != null;
                Elements docElements = document.getElementsByTag("DOC");
                for(Element element : docElements) {
                    Elements IDElement = element.getElementsByTag("DOCNO");
                    Elements DateElement = element.getElementsByTag("DATE1");
                    Elements TitleElement = element.getElementsByTag("TI");
                    Elements TextElement = element.getElementsByTag("TEXT");
                    cDocument newDoc = new cDocument(fileToSeparate.getName(), IDElement.text(), DateElement.text(), TitleElement.text(), TextElement.text());
                    docList.add(newDoc);
                }
                return docList;
            } catch(IOException e) {
                e.printStackTrace();
            }
            return docList;
        }
    }
}


