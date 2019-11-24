import Model.IO.ReadFile;
import Model.Structures.cDocument;

import java.util.concurrent.ArrayBlockingQueue;

public class forTest {

    public static void main(String[] args) {
        String cPath = "/home/serfati/Desktop/IRproject/corpus";
        java.util.concurrent.BlockingQueue<cDocument> bq = new ArrayBlockingQueue<cDocument>(50000);
        ReadFile rf = new ReadFile(cPath, bq);
        rf.readFiles();
    }

}