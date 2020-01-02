package Engine;

import Parser.MiniDictionary;
import Parser.Parse;
import Parser.cDocument;
import RW.Query;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.Callable;

public class Searcher implements Callable<LinkedList<String>> {
    Query q;
    String outputPath;
    boolean useStemming;
    boolean useSemantics;

    public Searcher(Query q, String outputPath, boolean useStemming, boolean useSemantics) {
        this.q = q;
        this.outputPath = outputPath;
        this.useStemming = useStemming;
        this.useSemantics = useSemantics;
    }

    @Override
    public LinkedList<String> call() {
        return getQueryResults();
    }

    private LinkedList<String> getQueryResults() {
        Parse p = new Parse(new cDocument("","","","",q.getQuery()),useStemming );
        MiniDictionary md  = p.parse();

        HashMap<String, Integer> wordsCountInQuery = null;

        for (String word: wordsCountInQuery.keySet()){

        }









        Ranker ranker = new Ranker();

        return null;
    }
}
