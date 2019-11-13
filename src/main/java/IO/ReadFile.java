package IO;

import Engine.Indexer;
import Parser.Parse;
import Structures.cDocument;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    private Queue<cDocument> documents;

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
        documents = new LinkedList<>();
        languages = new HashSet<>();
    }

    /**
     * This function  read the file  by send each file to thread
     */
    public void readFiles() {
        syncObject = new Object();
        numOfDocuments.addAndGet(files_list.length);
        for(File file : this.files_list)
            pool.execute(new Reader(file));

        synchronized (syncObject) {
            try {
                syncObject.wait();
            } catch(Exception e) {
                e.printStackTrace();
            } finally { //when all threads are done
                pool.shutdown();
            }
        }
    }

    private void writeDocumentsListToDisk(String pathForWriting) {
        if (!documents.isEmpty()) {
            StringBuilder documentsData = new StringBuilder();
            BufferedWriter writerDocuments;
            try {
                writerDocuments = new BufferedWriter(new FileWriter(pathForWriting+"\\documents.txt", true));
                writerDocuments.write("doc_name:title|date|city|max_tf|numOfUniqueTerms|length|entities"+"\n");

                cDocument document;
                while(!documents.isEmpty()) {
                    document = documents.poll();
                    documentsData.append(document.getDocId());
                    numOfDocuments.getAndIncrement();
                }
                writerDocuments.write(documentsData.toString());
                writerDocuments.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
            documents.clear();
        }
    }

    public ObservableList<String> getLanguages() {
        ObservableList<String> listOfLanguages = FXCollections.observableArrayList();
        listOfLanguages.addAll(languages);
        FXCollections.sort(listOfLanguages, new Indexer.StringComparator());
        return listOfLanguages;
    }


    public void loadLanguagesToDisk(String pathForWriting) {
        if (!languages.isEmpty()) {
            StringBuilder languagesData = new StringBuilder();
            BufferedWriter writeLanguages;
            try {
                writeLanguages = new BufferedWriter(new FileWriter(pathForWriting+"\\languages.txt", true));
                for(Iterator<String> iterator = languages.iterator(); iterator.hasNext(); ) {
                    String language = iterator.next();
                    languagesData.append(language).append("\n");
                }
                writeLanguages.write(languagesData.toString());
                writeLanguages.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
            languages.clear();
        }
    }


    /**
     * This class is thread that get file, split hum to cDocuments by JSOUP and send them to parse thread
     * All the jsoup code will esplain in the report.
     */
    class Reader implements Runnable {

        File file;

        Reader(File file) {
            this.file = file;
        }

        @Override
        public void run() {
            read(file);
        }

        void read(File file) {
            Document document = null;
            try {
                document = Jsoup.parse(new String(Files.readAllBytes(Objects.requireNonNull(file.listFiles())[0].toPath())));
            } catch(IOException e) {
                e.printStackTrace();
            }
            assert document != null;
            Elements docElements = document.getElementsByTag("DOC");//split by DOC tag
            document = null;
            cDocument[] docToParse = new cDocument[docElements.size()];
            int placeInDoc = 0;
            for(Element element : docElements) {
                Elements IDElement = element.getElementsByTag("DOCNO");
                Elements TitleElement = element.getElementsByTag("TI");
                Elements TextElement = element.getElementsByTag("TEXT");
                Elements fElements = element.getElementsByTag("F");
                String city = "";
                String language = "";
                for(Element fElement : fElements) {
                    if (fElement.attr("P").equals("104")) {//city
                        city = fElement.text();
                        if (city.length() > 0 && Character.isLetter(city.charAt(0)))
                            city = city.split(" ")[0].toUpperCase();
                        else
                            city = "";
                    } else if (fElement.attr("P").equals("105")) {//language
                        language = fElement.text().split(" ")[0];
                        if (!(!language.equals("") && !Character.isDigit(language.charAt(0)))) {
                            language = "";
                        }
                    }
                }
                String ID = IDElement.text();
                String title = TitleElement.text();
                String text = TextElement.text();
                cDocument cDoc = null;
                //cDoc = new cDocument();
                docToParse[placeInDoc++] = cDoc;
            }
            docElements.clear();
            for(cDocument d : documents)
                parser.parse(d.getDocText());
            numOfDocuments.getAndDecrement();
            if (numOfDocuments.get() == 0) synchronized (syncObject) {
                syncObject.notify();
            }

        }
    }
}