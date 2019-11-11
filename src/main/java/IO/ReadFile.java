package IO;

import Parser.Parse;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.jsoup.Jsoup.*;

/**
 * This class has the responsibility to get the corpus and split the file into documents.
 */
public class ReadFile implements IReader {

    private static AtomicInteger count = new AtomicInteger(0);
    Parse parser;
    private File[] files_list;
    private ExecutorService pool;

    /**
     * c'tor
     *
     * @param corpusPath    - the path of corpus with files
     * @param postingOut    - the path to directory we wrute the posting files.
     * @param ifStem        - if to do stem
     */
    public ReadFile(String corpusPath, String postingOut, boolean ifStem) {
        File corpus = new File(corpusPath);
        files_list = corpus.listFiles();
        //parser = new Parse(corpusPath postingOut ifStem);
        pool = Executors.newFixedThreadPool(8);
    }

    /**
     * This function  read the file  by send each file to thread
     */
    public void read() {
        /* use to sync the read file*/
        count.addAndGet(files_list.length);
        Object syncObject = new Object();

        File[] files_list1 = this.files_list;
        for(int i = 0, files_list1Length = files_list1.length; i < files_list1Length; i++) {
            File file = files_list1[i];
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

    @Override
    public boolean getDone() {
        return false;
    }

    @Override
    public void reset() {

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
        {
            Document document = null;
            try {
                document = parse(new String(Files.readAllBytes(Objects.requireNonNull(file.listFiles())[0].toPath())));
            } catch(IOException e) {
                e.printStackTrace();
            }
            assert document != null;

            Elements docElements = document.getElementsByTag("DOC");
            Document[] docsToParse;
            int placeInDoc = 0;
            for(Element element : docElements) {
                Elements IDElement = element.getElementsByTag("DOCNO");
                Elements TitleElement = element.getElementsByTag("TI");

                Elements TextElement = element.getElementsByTag("TEXT");
                Elements fElements = element.getElementsByTag("F");

                for(Element fElement : fElements) {

                }

                String ID = IDElement.text();
                String title = TitleElement.text();
                String text = TextElement.text();
                Document Doc;
            }
        }
    }
}
