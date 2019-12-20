package RW;

import Parser.cDocument;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ReadFile {

    /**
     * this function reads a bunch of files
     * @param pathOfDocs the path of the corpus
     * @return a list of Corpus Document
     */
    public LinkedList<cDocument> readFiles(String pathOfDocs, int j, int jumps) {
        File[] directoryListing = new File(pathOfDocs).listFiles();
        LinkedList<cDocument> allDocsInCorpus = new LinkedList<>();
        if (directoryListing != null) {
            int start = j * directoryListing.length / jumps;
            int end = (j+1) * directoryListing.length / jumps;
            ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
            LinkedList<Future<LinkedList<cDocument>>> futureDocsInFile = IntStream.rangeClosed(start, end-1)
                    .mapToObj(i -> pool.submit(new reader(directoryListing[i]))).collect(Collectors.toCollection(LinkedList::new));

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

    //------------------------- Callable thread-----------------------------------------//

    class reader implements Callable<LinkedList<cDocument>> {

        private File file;
        private LinkedList<cDocument> docList;

        reader(File fileToSeparate) {
            this.file = fileToSeparate;
        }

        /**
         * goes throw all the files in a certain folder and read documents
         * @return a list of the corpus document exist in a file
         */
        public LinkedList<cDocument> call() {
            File[] directoryListing = file.listFiles();
            LinkedList<cDocument> fileList = new LinkedList<>();
            if (!(directoryListing == null || !file.isDirectory()))
                Arrays.stream(directoryListing).map(this::jsoupRead).forEachOrdered(fileList::addAll);
            return fileList;
        }

        private LinkedList<cDocument> jsoupRead(File fileToSeparate) {
           docList = new LinkedList<>();
            try {
                Document doc = Jsoup.parse(new FileInputStream(fileToSeparate), null, "", Parser.xmlParser());
                Elements elements = doc.select("DOC");
                //get contents of all tags in a specific doc
                elements.forEach(element -> {
                    String docID = element.getElementsByTag("DOCNO").text();
                    String docText = element.getElementsByTag("TEXT").text();
                    String docTitle = element.getElementsByTag("TI").text();
                    String docDate = element.getElementsByTag("DATE1").text();
                    cDocument document = new cDocument(fileToSeparate.getName(), docID, docDate, docTitle, docText);
                    docList.add(document);
                });
                return docList;
            } catch(IOException e) {
                e.printStackTrace();
            }
            return docList;
        }
    }

    /**
     * initiate the stop words set containing all the stop words
     * @param fileName the path of the file
     */
    public static HashSet<String> initStopWordsSet(String fileName) {
        HashSet<String> set = new HashSet<>();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
            set = bufferedReader.lines().collect(Collectors.toCollection(HashSet::new));
            bufferedReader.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
        return set;
    }
}
