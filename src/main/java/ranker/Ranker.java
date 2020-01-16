package ranker;

import indexer.DocumentIndex;
import javafx.util.Pair;
import model.Model;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import static ranker.Searcher.addToScore;

/* ranker class . gets a list of terms and ranks all relevant documents by them by the weights set below*/
public class Ranker {

    double BM_25_B = 0.7, BM_25_K = 1.5;
    double TITLE_WEIGHT = 0.15, POSITIONS_WEIGHT = 0.08, BM25_WEIGHT = 0.78, POS_PERCENT = 15, CONTAINING_WEIGHT = 0.4;
    double averageDocumentLength;
    private HashMap<String, Integer> wordsCountInQuery;

    Ranker(HashMap<String, Integer> wordsCount) {
        this.averageDocumentLength = getDocumentAverageLength();
        this.wordsCountInQuery = wordsCount;
    }

    double BM25Algorithm(String word, String documentName, int tf, double idf) {
        BM_25_K = 1.2;
        BM_25_B = 0.75;
        int documentLength = Model.documentDictionary.get(documentName).getDocLength();
        int wordInQueryCount = wordsCountInQuery.get(word);
        double numeratorBM25 = wordInQueryCount * (BM_25_K+1) * tf * idf;
        double denominatorBM25 = tf+BM_25_K * (1-BM_25_B+(BM_25_B * (documentLength / averageDocumentLength)));
        return BM25_WEIGHT * numeratorBM25 / denominatorBM25;
    }

    /**
     * calculates the doc title with the query
     *
     * @param score    map of scores
     * @param docName  document name
     * @param wordsSet words of query
     */
    void titleAlgorithm(HashMap<String, Double> score, String docName, Set<String> wordsSet) {
        String title = Model.documentDictionary.get(docName).getDocTI().toLowerCase();
        if (!title.equals(""))
            Arrays.stream(StringUtils.split(title, " ~;!?=#&^*+\\|:\"(){}[]<>\n\r\t")).filter(wordsSet::contains).forEach(wordTitle -> addToScore(score, docName, TITLE_WEIGHT * 0.1));
    }

    /**
     * adds to the score the 5 entities algorithm
     *
     * @param score        score map
     * @param wordsInQuery words of query
     */
    void containingAlgorithm(HashMap<String, Double> score, Set<String> wordsInQuery) {
        for(String docName : score.keySet()) {
            if (Model.documentDictionary.get(docName).getFiveEntities() != null) {
                for(Pair<String, Integer> aFive : Model.documentDictionary.get(docName).getFiveEntities()) {
                    if (aFive != null && wordsInQuery.contains(aFive.getKey())) {
                        addToScore(score, docName, CONTAINING_WEIGHT * 0.1);
                    }
                }
                if (wordsInQuery.contains(Model.documentDictionary.get(docName).getMaxFreq_word()))
                    addToScore(score, docName, CONTAINING_WEIGHT * 0.5);
            }
        }
    }


    double positionsAlgorithm(HashMap<String, Double> score, Set<String> wordsInQuery) {
        double rank = 0;
//        for(String docName : score.keySet()) {
//            if (Model.documentDictionary.get(docName). != null) {
//
//            }
//        }
        return POSITIONS_WEIGHT * rank;
    }

    /**
     * returns the average document length
     *
     * @return the average length
     */
    private double getDocumentAverageLength() {
        return Model.documentDictionary.values().stream().mapToDouble(DocumentIndex::getDocLength).sum() / Model.documentDictionary.size();
    }
}
