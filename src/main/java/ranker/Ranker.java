package ranker;

import indexer.DocumentIndex;
import javafx.util.Pair;
import model.Model;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

/**
 * ranker class . gets a list of terms and ranks all relevant documents by them by the weights set below
 */

public class Ranker {

    private double BM_25_B = 0.7, BM_25_K = 1.5, CONTAINING_WEIGHT = 0.4;
    private double TITLE_WEIGHT = 0.45, POSITIONS_WEIGHT = 0.08, BM25_WEIGHT = 0.25;
    double m_averageDocumentLength;//average document size in the corpus
    private HashMap<String, Integer> wordsCountInQuery;//count of words in the query

    Ranker(HashMap<String, Integer> wordsCount) {
        this.m_averageDocumentLength = getDocumentAverageLength();
        this.wordsCountInQuery = wordsCount;
    }

    double BM25Algorithm(String word, String documentName, int tf, double idf) {
        BM_25_K = 1.2;
        BM_25_B = 0.75;
        int documentLength = Model.documentDictionary.get(documentName).getDocLength();
        int wordInQueryCount = wordsCountInQuery.get(word);
        double numeratorBM25 = wordInQueryCount * (BM_25_K+1) * tf * idf;
        double denominatorBM25 = tf+BM_25_K * (1-BM_25_B+(BM_25_B * (documentLength / m_averageDocumentLength)));
        return BM25_WEIGHT * numeratorBM25 / denominatorBM25;
    }

    /**
     * calculates the doc title with the query
     *
     * @param docName  document name
     * @param wordsSet words of query
     */
    double titleAlgorithm(String docName, Set<String> wordsSet) {
        //go throw all words of title and check if words appear in the query
        double rank = 0;
        String title = Model.documentDictionary.get(docName).getDocTI().toLowerCase();
        if (!title.equals("")) {
            String[] split = StringUtils.split(title, " ~;!?=#&^*+\\|:\"(){}[]<>\n\r\t");
            rank = Arrays.stream(split).filter(wordsSet::contains).mapToDouble(wordTitle -> 0.1).sum();
        }
        return TITLE_WEIGHT * rank;
    }

    /**
     * adds to the score the 5 entities algorithm
     *
     * @param score        score map
     * @param wordsInQuery words of query
     */
    double containingAlgorithm(HashMap<String, Double> score, Set<String> wordsInQuery) {
        double rank = 0;
        for(String docName : score.keySet()) {
            Pair<String, Integer>[] five = Model.documentDictionary.get(docName).getFiveEntities();
            if (five != null)
                rank += Arrays.stream(five).filter(aFive -> aFive != null && wordsInQuery.contains(aFive.getKey())).mapToDouble(aFive -> 0.1).sum();
        }
        return CONTAINING_WEIGHT * rank;
    }

    double positionsAlgorithm(HashMap<String, Double> score, Set<String> wordsInQuery) {
        final int POS_PERCENT = 15;
        double rank = 0;
        for(String docName : score.keySet()) {
            Pair<String, Integer>[] five = Model.documentDictionary.get(docName).getFiveEntities();
            if (five != null)
                rank += Arrays.stream(five).filter(aFive -> aFive != null && wordsInQuery.contains(aFive.getKey())).mapToDouble(aFive -> 0.1).sum();
        }
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
