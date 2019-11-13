package IO;

import Parser.Parse;
import Structures.cDocument;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jsoup.*;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Document;

import java.io.*;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class has the responsibility to get the corpus and splite the file into cDocuments.
 */
public class ReadFile {
    /**
     * nunber if reader that done
     */
    private static AtomicInteger numOfDocuments = new AtomicInteger(0);
    /**
     * list of files
     */
    private File[] files_list = null;
    private Parse parser;
    /**
     * pool of thread that rin read function
     */
    private ExecutorService pool;
    /**
     * use to sync the read file
     */
    private Object syncObject;
    private Queue<cDocument> cDocuments;

    private HashSet<String> languages;

    private int numOfParsedDocs;


    /**
     * c'tor
     *
     * @param corpusPath    - the path of corpus with files
     * @param stopWordsPath - the path for stop words
     * @param postingOut    - the path to directory we wrute the posting files.
     * @param ifStem        - if to do stem
     */
    public ReadFile(String corpusPath, String stopWordsPath, String postingOut, boolean ifStem) {
        File corpus = new File(corpusPath);
        files_list = corpus.listFiles();
        //parser = new Parse(corpusPath,stopWordsPath,postingOut,ifStem);
        pool = Executors.newFixedThreadPool(8);

        cDocuments = new LinkedList<>();

        languages = new HashSet<>();



    }

    /**
     * This function  read the file  by send each file to thread
     */
    public void readFiles() {
        syncObject = new Object();
        numOfDocuments.addAndGet(files_list.length);
        for(File file : this.files_list) {
            pool.execute(new Reader(file));
        }
        synchronized (syncObject) {
            try {
                syncObject.wait();
            } catch(Exception e) {
                e.printStackTrace();
            } finally {//when all threads are done
                pool.shutdown();
            }
        }
    }

    private void writeDocumentsListToDisk(String pathForWriting) {
        cDocument cDocument = null;
        if (!cDocuments.isEmpty()) {
            StringBuilder documentsData = new StringBuilder();
            BufferedWriter writerDocuments = null;
            try {
                writerDocuments = new BufferedWriter(new FileWriter(pathForWriting+"/cDocuments.txt", true));
                writerDocuments.write("doc_name:title|date|max_tf|numOfUniqueTerms|length|entities"+"\n");

                while(!cDocuments.isEmpty()) {
                    cDocument = cDocuments.poll();
                    documentsData.append(cDocument.getDocId()+""+cDocument.getDocDate()+" "); //TODO
                    numOfDocuments.getAndIncrement();
                }
                writerDocuments.write(documentsData.toString());
                writerDocuments.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
            cDocuments.clear();
        }
    }

    public ObservableList<String> getLanguages() {
        BufferedReader bw = null;
        try {

            bw = new BufferedReader(new FileReader(parser.getPathToWrite()+"/Languages.txt"));

        } catch(IOException e) {
            e.printStackTrace();
        }


    }


    public void loadLanguagesToDisk(String pathForWriting) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(pathForWriting+"/Languages.txt", true));
            for(String lan : languages)
                bw.write(lan);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * This class is thread that get file, split hum to cDocuments by JSOUP and send them to parse thread
     * All the jsoup code will esplain in the report.
     */
    class Reader implements Runnable {

        File file;

        public Reader(File file) {
            this.file = file;
        }

        @Override
        public void run() {
            read(file);
        }

        void read(File file) {
            Document cDocument = null;
            try {
                cDocument = Jsoup.parse(new String(Files.readAllBytes(Objects.requireNonNull(file.listFiles())[0].toPath())));
            } catch(IOException e) {
                e.printStackTrace();
            }
            assert cDocument != null;
            Elements docElements = cDocument.getElementsByTag("DOC");   //split by DOC tag
            cDocument = null;
            cDocument[] docToParse = new cDocument[docElements.size()];
            int placeInDoc = 0;
            for(Element element : docElements) {
                Elements IDElement = element.getElementsByTag("DOCNO");
                Elements TitleElement = element.getElementsByTag("TI");
                Elements dateElement = element.getElementsByTag("DATE");
                Elements authorElement = element.getElementsByTag("BYLINE");
                Elements TextElement = element.getElementsByTag("TEXT");
                Elements fElements = element.getElementsByTag("F");
                String language = "";
                for(Element fElement : fElements) {
                    if (fElement.attr("P").equals("105")) {
                        language = fElement.text().split(" ")[0];
                        if (!(!language.equals("") && !Character.isDigit(language.charAt(0)))) {
                            language = "";
                        }
                    }
                }
                String ID = IDElement.text();
                String title = TitleElement.text();
                String text = TextElement.text();
                String author = authorElement.text();
                String date = dateElement.text();
                cDocument doc = new cDocument(ID, date, title, text, author, language);
                cDocuments.add(doc);
                if (language != "")
                    languages.add(language);
                parser.parse(doc);
                numOfParsedDocs++;

                if (numOfParsedDocs == 100) {
                    writeDocumentsListToDisk(parser.getPathToWrite());
                    numOfParsedDocs = 0;
                }
            }
            //arriving threshold
            docElements.clear();
            numOfDocuments.getAndDecrement();
            if (numOfDocuments.get() == 0) {
                synchronized (syncObject) {
                    syncObject.notify();
                }
            }

        }
    }
}
