package ranker;

import indexer.InvertedIndex;
import javafx.util.Pair;
import parser.cDocument;

import java.util.ArrayList;
import java.util.HashMap;

/* contract for being a ranker*/
public interface IRanker {

    /* the main ranking function*/
    ArrayList<cDocument> rankByTerms(ArrayList<InvertedIndex.Term> queryWords);

    /* set the paths to the postings lists*/
    void setAttributes(String termsPath, String docsPath, double docLength);

    /* set the dictionary . usefull when the dictionaries are loaded from memor */
    void setDictionaries(HashMap<Integer, Pair> docPositions);
}
