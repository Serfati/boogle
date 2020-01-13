package ranker.algorithms;

import indexer.DocumentIndex;
import model.Model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

/* an algorithm to rank by positions in the document */

public class PositionsAlgorithm extends ARankingAlgorithm {

    private final int POS_PERCENT = 15;

    public PositionsAlgorithm(double weight) {
        super(weight);
    }


    public double rank(HashMap<String, Double> score, String docName, Set<String> wordsInQuery) {
        double rank = 0;
        DocumentIndex currDoc = Model.documentDictionary.get(docName);
        if (currDoc == null || wordsInQuery == null || wordsInQuery.size() == 1)
            return 0;

        LinkedList<Integer> positions = new LinkedList<>();
        int docLen = currDoc.getDocLength();


        //this is the last position of the important section of the document
        int lastImportentPosition = (docLen * POS_PERCENT) / 100;


        return rank * weight;
    }
}





