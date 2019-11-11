package Model;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class has the responsibility to get the corpus and splite the file into documents.
 */
public class ReadFile {
    /**
     * nunber if reader that done
     */
    private static AtomicInteger count = new AtomicInteger(0);
    Parse parser;
    /**
     * list of files
     */
    private File[] files_list = null;
    /**
     * pool of thread that rin read function
     */
    private ExecutorService pool;

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
        //parser = new Parse();
        pool = Executors.newFixedThreadPool(8);
    }

    /**
     * This function  read the file  by send each file to thread
     */
    public void readFiles() {
        /* use to sync the read file*/
        Object syncObject = new Object();
        count.addAndGet(files_list.length);
        for(File file : this.files_list) {
            pool.execute(new Reader(file));
        }
        synchronized (syncObject) {
            try {
                syncObject.wait();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}

/**
 * This class is thread that get file, split hum to documents by JSOUP and send them to parse thread
 * All the jsoup code will explain in the report.
 */
class Reader implements Runnable {

    private File file;

    Reader(File file) {
        this.file = file;
    }

    @Override
    public void run() {
        read(file);
    }

    private void read(File file) {
        //TODO
    }

}


