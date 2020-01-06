package ranker.algorithms;

import indexer.InvertedIndex;
import parser.MiniDictionary;

import java.util.ArrayList;

/* check if terms are at the title of the given document */
public class TitleAlgorithm extends ARankingAlgorithm {

    public TitleAlgorithm(double weight) {
        super(weight);
    }

    @Override
    public double rank(MiniDictionary document, ArrayList<InvertedIndex.Term> termList) {
        return termList.stream().filter(term -> document.getTitle().contains(term.toString())).mapToDouble(term -> weight).sum();
    }
}
