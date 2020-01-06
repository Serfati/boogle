package rw;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import parser.cDocument;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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

    public static LinkedList<Query> readQueries(File queries) {
        FileInputStream fis;
        LinkedList<Query> queryList = null;
        try {
            fis = new FileInputStream(queries);
            Document doc = Jsoup.parse(fis, null, "", Parser.xmlParser());
            Elements elements = doc.select("top");
            queryList = new LinkedList<>();
            for(Element element : elements) {
                String numberWithOtherData = element.getElementsByTag("num").text();
                //String queryNum = getNumberOfQuery(numberWithOtherData);
                String num = "Number: ";
                int startNumIndex = numberWithOtherData.indexOf(num)+num.length();
                int endNumIndex = numberWithOtherData.indexOf(' ', startNumIndex+1);
                String queryNum = numberWithOtherData.substring(startNumIndex, endNumIndex);

                String titleWithOtherData = element.getElementsByTag("title").text();
                String title = titleWithOtherData.substring(0, titleWithOtherData.indexOf("\n")-1);

                String descWithOtherData = element.getElementsByTag("desc").text();
                String description = "Description: \n";
                int startDescIndex = descWithOtherData.indexOf(description)+description.length();
                int endDescIndex = descWithOtherData.indexOf("Narrative");
                String desc = descWithOtherData.substring(startDescIndex, endDescIndex-3);

                String narrWithOtherData = element.getElementsByTag("narr").text();
                String narrative = "Narrative: \n";
                String narr = narrWithOtherData.substring(narrWithOtherData.indexOf(narrative)+narrative.length());
                queryList.add(new Query(queryNum, title, desc));
                fis.close();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        return queryList;
    }

    public static LinkedList<String> readPostingLineAtIndex(String path, char c, List<Integer> indexes, boolean stem){
        String filePath = path + ( stem ? "\\Stemmed\\" : "\\Unstemmed\\");
        filePath += "_"+ c +".txt";
        File postingFile = new File (filePath);
        LinkedList<String> postingLines = new LinkedList<>();
        try {
            List l = FileUtils.readLines(postingFile);
            for (Integer i : indexes) postingLines.add(l.get(i).toString());
        } catch(IOException e) {
            e.printStackTrace();
        }
        return postingLines;
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
