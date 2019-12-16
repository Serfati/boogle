import Model.Engine.DocDictionaryNode;
import Model.Engine.InvertedIndex;
import Model.IO.ReadFile;
import Model.Parser.cDocument;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class testClass {

    public static void main(String[] args) {
        ReadFile rf = new ReadFile();
        InvertedIndex invertedIndex;
        HashMap<String, DocDictionaryNode> documentDictionary;
        HashSet<String> stopWords;
        stopWords = ReadFile.initSet("/home/serfati/Desktop/1/stop_words.txt");
        invertedIndex = new InvertedIndex();
        documentDictionary = new HashMap<>();
        int numOfDocs = 0;
        int numOfTempPostings = 900;
        LinkedList<Thread> tmpPostingThread = new LinkedList<>();
        LinkedList<cDocument> l = null;
        for(int i = 0; i < numOfTempPostings; i++) {
            l = ReadFile.readFiles("/home/serfati/Desktop/1", i, 900);
        }
        System.out.println(l.size());

    }
}
