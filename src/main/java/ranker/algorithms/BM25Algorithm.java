package ranker.algorithms;

import indexer.DocumentIndex;
import indexer.InvertedIndex;
import model.Model;
import parser.MiniDictionary;

import java.util.ArrayList;
import java.util.HashMap;

/* implementation of the bm25 okapi algorithm */

public class BM25Algorithm extends ARankingAlgorithm {
    private double m_averageDocumentLength;//average document size in the corpus
    private HashMap<String, Integer> m_wordsCount;//count of words in the query

    private double k, b, avgDocLength, totalDocCount;

    public BM25Algorithm(double avgDocLength, double totalDocCount, double weight, double b, double k) {
        super(weight);
        this.totalDocCount = totalDocCount;
        this.avgDocLength = avgDocLength;
        this.b = b;
        this.k = k;
    }

    @Override
    public double rank(MiniDictionary document, ArrayList<InvertedIndex.Term> termList) {
        double docRank = 0;
        for(InvertedIndex.Term t : termList)
            docRank += BM25(t.getWord(), document.getName(), t.getTermFreq(), t.getNumOfAppearances());
        return docRank * weight;
    }

    double BM25(String word, String documentName, int tf, double idf) {
        int documentLength = Model.documentDictionary.get(documentName).getDocLength();
        int wordInQueryCount = m_wordsCount.get(word);
        double numeratorBM25 = wordInQueryCount * (k+1) * tf * idf;
        double denominatorBM25 = tf+k * (1-b+(b * (documentLength / m_averageDocumentLength)));
        return numeratorBM25 / denominatorBM25;
    }

    /**
     * returns the average document length
     *
     * @return the average length
     */
    private double getDocumentAverageLength() {
        double sum = 0, count = 0;
        for(DocumentIndex node : Model.documentDictionary.values()) {
            sum += node.getDocLength();
            count++;
        }
        return sum / count;
    }

    public void setTotalDocCount(int docCount) {
        this.totalDocCount = docCount;
    }

    public void setK(double k) {
        this.k = k;
    }

    public void setb(double b) {
        this.b = b;
    }

    /**
     * returns the IDF of a word in the corpus
     *
     * @param length number of docs the word appears in
     * @return calculated IDF
     */
    private Double getIDF(int length) {
        double docInCorpusCount = Model.documentDictionary.keySet().size();
        return Math.log10((docInCorpusCount+1) / length);
    }
}
