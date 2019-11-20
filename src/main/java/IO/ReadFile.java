package IO;

import Structures.cDocument;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
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
            int placeinArray = 0;
            assert document != null;
            Elements docElements = document.getElementsByTag("DOC"); //split by DOC tag
            for(Element element : docElements) {
                Elements IDElement = element.getElementsByTag("DOCNO");
                Elements DateElement = element.getElementsByTag("DATE1");
                Elements unProcessedDocAuthor = element.getElementsByTag("BYLINE");
                Elements TitleElement = element.getElementsByTag("TI");
                Elements TextElement = element.getElementsByTag("TEXT");
                Elements fElements = element.getElementsByTag("F");
                String orginC = "";
                String language = "";
                for(Element fElement : fElements) {
                    if (fElement.attr("P").equals("104")) {//orginC
                        orginC = fElement.text();
                        if (orginC.length() > 0 && Character.isLetter(orginC.charAt(0)))
                            orginC = orginC.split(" ")[0].toUpperCase();
                        else
                            orginC = "";
                    } else if (fElement.attr("P").equals("105")) {//language
                        language = fElement.text().split(" ")[0];
                        if (!(!language.equals("") && !Character.isDigit(language.charAt(0)))) {
                            language = "";
                        }
                    }
                }
                String ID = IDElement.text();
                String DATE = DateElement.text();
                String TILTLE = TitleElement.text();
                String TEXT = TextElement.text();
                String author = unProcessedDocAuthor.text().replace("By ", "");
                cDocument newDoc = new cDocument(ID, DATE, TILTLE, TEXT, orginC, author, language);
                documentBuffer.offer(newDoc);
                //docsToparse[placeinArray++] = newDoc;

            }

            if (documentBuffer.size() > 500) {
                for(cDocument d : documentBuffer)
                    parse(d.getDocText());
                    //writeDocumentsListToDisk(corpusPath);
                    //System.out.println("done parse 500 docs");


            }
            docElements.clear();
            numOfDocuments.getAndDecrement();
            if (numOfDocuments.get() == 0) synchronized (syncObject) {
                syncObject.notify();
            }
        }
    }


//    private void writeDocumentsListToDisk(String pathForWriting) {
//        if (!docsToWrite.isEmpty()) {
//            StringBuilder documentsData = new StringBuilder();
//            BufferedWriter writerDocuments;
//            try {
//                writerDocuments = new BufferedWriter(new FileWriter(pathForWriting+"/cReview.txt", true));
//                //writerDocuments.write("[ doc_ID ] : | date | -  title  - |  lang  | max_tf | numOfUniqueTerms | length | "+"\n");
//
//                cDocument document;
//                while(!docsToWrite.isEmpty()) {
//                    document = docsToWrite.poll();
//
//                    documentsData.append(document.getDocId()).append(";;")
//                            .append(document.getDocDate())
//                            .append("|").append(document.getDocTitle()).append("|").
//                            append(document.getDocLang()).append("|").append(document.getMaxFrequency()).
//                            append("|").append(document.getNumOfUniqueTerms()).append("|").
//                            append("\n");
//                    numOfDocuments.getAndIncrement();
//                }
//                writerDocuments.write(documentsData.toString());
//                writerDocuments.close();
//            } catch(Exception e) {
//                e.printStackTrace();
//            }
//            docsToWrite.clear();
//        }
//    }

//    public ObservableList<String> getLanguages() {
//        ObservableList<String> listOfLanguages = FXCollections.observableArrayList();
//        listOfLanguages.addAll(languages);
//        FXCollections.sort(listOfLanguages, new Indexer.StringComparator());
//        return listOfLanguages;
//    }

}