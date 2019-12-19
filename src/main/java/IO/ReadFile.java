package IO;

import Parser.cDocument;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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

    public static HashSet<String> initStopWordsSet(String fileName) {
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

    public LinkedList<cDocument> readFiles(String pathOfDocs, int j, int jumps) {
        File[] directoryListing = new File(pathOfDocs).listFiles();
        LinkedList<cDocument> allDocsInCorpus = new LinkedList<>();
        if (directoryListing != null) {
            int start = j * directoryListing.length / jumps;
            int end = (j+1) * directoryListing.length / jumps;
            ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
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

        reader(File fileToSeparate) {
            this.file = fileToSeparate;
        }

        /**
         * goes throw all the files in a certain folder and read documents
         *
         * @return a list of the corpus document exist in a file
         */
        public LinkedList<cDocument> call() {
            File[] directoryListing = file.listFiles();
            LinkedList<cDocument> fileList = new LinkedList<>();
            if (directoryListing != null && file.isDirectory())
                Arrays.stream(directoryListing).map(this::jsoupRead).forEachOrdered(fileList::addAll);
            return fileList;
        }

        private LinkedList<cDocument> jsoupRead(File fileToSeparate) {
            LinkedList<cDocument> docList = new LinkedList<>();
            try {
                FileInputStream fis = new FileInputStream(fileToSeparate);
                Document doc = Jsoup.parse(fis, null, "", Parser.xmlParser());
                Elements elements = doc.select("DOC");
                //get contents of all tags in a specific doc
                for(Element element : elements) {
                    String docNum = element.getElementsByTag("DOCNO").text();
                    String docDate = element.getElementsByTag("DATE1").text();
                    String docText = element.getElementsByTag("TEXT").text();
                    String docTitle = element.getElementsByTag("TI").text();
                    cDocument document = new cDocument(fileToSeparate.getName(), docNum, docDate, docTitle, docText);
                    docList.add(document);
                }
                return docList;
            } catch(IOException e) {
                e.printStackTrace();
            }
            return docList;
        }
    }
}
