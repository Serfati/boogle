package ranker.algorithms;

import indexer.InvertedIndex;
import parser.MiniDictionary;

import java.util.ArrayList;
import java.util.LinkedList;

/* an algorithm to rank by positions in the document */

public class PositionsAlgorithm extends ARankingAlgorithm {

    private final int POS_PERCENT = 15;

    public PositionsAlgorithm(double weight) {
        super(weight);
    }


    public double rank(MiniDictionary document, ArrayList<InvertedIndex.Term> queryWords) {

        if (document == null || queryWords == null || queryWords.size() == 1)
            return 0;

        double rank = 0;
        String docID = document.getName();
        LinkedList<Integer> positions = new LinkedList<>();
        int docLen = document.getDocLength();

        //this is the last position of the important section of the document
        int lastImportentPosition = (docLen * POS_PERCENT) / 100;

        //TODO

        return rank * weight;
    }
}





