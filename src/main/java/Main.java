import IO.ReadFile;
import Structures.cDocument;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Main {

    public static void main(String[] args) {


        System.out.print("Hello Yarden Good luck!! :)\n!");

        String cPath = "/home/serfati/Desktop/IRproject/corpus";
        BlockingQueue<cDocument> bq = new ArrayBlockingQueue<>(50);
        ReadFile rf = new ReadFile(cPath, bq);
        rf.readFiles();

    }
}
