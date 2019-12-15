package Model.IO;

import Model.Engine.Indexer;
import Model.Engine.InvertedIndex;
import Model.Parser.cDocument;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class has the responsibility to get the corpus and splite the file into cDocuments.
 */
public class ReadFile {
    /* number if reader that done */
    private static AtomicInteger numOfDocuments = new AtomicInteger(0);
    /* use to sync the read file */
    private final Object syncObject = new Object();
    private String corpusPath;
    private BlockingQueue<cDocument> documentBuffer;
    /* pool of thread that rin read function */
    private ExecutorService pool;


    /**
     * c'tor
     * @param corpusPath    - the path of corpus with files
     */
    public ReadFile(String corpusPath, BlockingQueue<cDocument> documentBuffer) {
        this.corpusPath = corpusPath;
        pool = Executors.newFixedThreadPool(4);
    }

    /* This function  read the file  by send each file to thread */
    public void readFiles() {
        File corpus = new File(corpusPath);
        FileFilter subDirsFiller = File::isDirectory;
        File[] files_list = corpus.listFiles(subDirsFiller);
        assert files_list != null;
        numOfDocuments.addAndGet(files_list.length);
        for(File file : files_list)
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

    /* This class is thread that get file, split hum to cDocuments by JSOUP and send them to parse thread
     * All the jsoup code will esplain in the report.*/
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
            Elements docElements = document.getElementsByTag("DOC"); //split by DOC tag
            for(Element element : docElements) {
                Elements IDElement = element.getElementsByTag("DOCNO");
                Elements DateElement = element.getElementsByTag("DATE1");
                Elements TitleElement = element.getElementsByTag("TI");
                Elements TextElement = element.getElementsByTag("TEXT");
                String docLang = element.getElementsByTag("F").select("F[P=105]").text().toUpperCase();
                cDocument newDoc = new cDocument(IDElement.text(), DateElement.text(), TitleElement.text(), TextElement.text(), docLang);
                documentBuffer.offer(newDoc);
            }

            if (documentBuffer.size() > 500) {
                for(cDocument d : documentBuffer)
                    //writeDocumentsListToDisk(corpusPath);
                    System.out.println("done parse 500 docs");
            }
            docElements.clear();
            numOfDocuments.getAndDecrement();
            if (numOfDocuments.get() == 0) synchronized (syncObject) {
                syncObject.notify();
            }
        }
    }

}

