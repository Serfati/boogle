import Model.Engine.DocDictionaryNode;
import Model.Engine.InvertedIndex;
import Model.Engine.MiniDictionary;
import Model.IO.ReadFile;
import Model.IO.WriteFile;
import Model.Parser.Parse;
import Model.Parser.cDocument;
import javafx.util.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class testClass {

    public static void main(String[] args) {
        ReadFile rf = new ReadFile();
        HashMap<String, DocDictionaryNode> documentDictionary;
        HashSet<String> stopWords;
        stopWords = ReadFile.initSet("/home/serfati/Desktop/1/stop_words.txt");
        documentDictionary = new HashMap<>();
        int numOfDocs = 0;
        int numOfTempPostings = 15;
        LinkedList<Thread> tmpPostingThread = new LinkedList<>();
        LinkedList<cDocument> l = null;
        InvertedIndex index;
        AtomicInteger numOfPostings = new AtomicInteger(0);
        for(int i = 0; i < numOfTempPostings; i++) {
            l = ReadFile.readFiles("/home/serfati/Desktop/1", i, numOfTempPostings);

            ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
            ConcurrentLinkedDeque<Future<MiniDictionary>> futureMiniDicList = l.stream().map(cd -> pool.submit(new Parse(cd, false))).collect(Collectors.toCollection(ConcurrentLinkedDeque::new));
            try {
                ConcurrentLinkedDeque<MiniDictionary> miniDicList = new ConcurrentLinkedDeque<>();
                for(Future<MiniDictionary> fMiniDic : futureMiniDicList) {
                    miniDicList.add(fMiniDic.get());
                    numOfDocs++;
                }
                index = new InvertedIndex(miniDicList);
                Future<HashMap<String, Pair<Integer, StringBuilder>>> futureTemporaryPosting = pool.submit(index);
                HashMap<String, Pair<Integer, StringBuilder>> temporaryPosting = futureTemporaryPosting.get();

                Thread t1 = new Thread(() -> WriteFile.writeTempPosting("/home/serfati/Desktop/2", numOfPostings.getAndIncrement(), temporaryPosting));
                t1.start();

                tmpPostingThread.add(t1);
                pool.shutdown();
            } catch(InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Done");
    }
}
