package ranker;

import parser.MiniDictionary;
import parser.Parse;
import parser.cDocument;
import rw.Query;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.Callable;

public class Searcher implements Callable<LinkedList<String>> {
    Query q;
    String outputPath;
    boolean useStemming;
    boolean useSemantics;
    private final int DOCS_RETURN_NUMBER = 50;

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
        Parse p = new Parse(new cDocument("", "", "", "", q.getQueryText()), useStemming);
        MiniDictionary md = p.parse();

        HashMap<String, Integer> wordsCountInQuery = null;

        Ranker ranker = null;

        return null;
    }

}
