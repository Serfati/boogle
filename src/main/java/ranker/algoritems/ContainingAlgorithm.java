package ranker.algoritems;

import indexer.InvertedIndex;
import parser.MiniDictionary;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * checks if a term is an entity of the given document .
 */

public class ContainingAlgorithm extends ARankingAlgorithm {

    public ContainingAlgorithm(double weight) {
        super(weight);
    }

    @Override
    public double rank(MiniDictionary document, ArrayList<InvertedIndex.Term> termList) {

        double rank = 0;
        LinkedList<Integer> positions;
        int inDoc = 0, numTerms = 0;

        for(InvertedIndex.Term t : termList) {

            //TODO
            rank += 0.3;
        }
        return rank * weight;
    }
}
