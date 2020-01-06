package ranker.algorithms;

import indexer.InvertedIndex.Term;
import parser.MiniDictionary;

import java.util.ArrayList;

/* abstract class to be extended by each ranking algorithm that's to be used by the ranker*/

public abstract class ARankingAlgorithm {

    protected double weight;

    public ARankingAlgorithm(double weight) {
        this.weight = weight;
    }

    /**
     * rank a document according to a term list given from a query
     *
     * @param document   current document that is being ranked
     * @param queryWords the list of terms from the query
     * @return the ranking value of the algorithm
     */
    public abstract double rank(MiniDictionary document, ArrayList<Term> queryWords);

}
