package ranker.algorithms;

import model.Model;

/* implementation of the bm25 okapi algorithm */

public class BM25Algorithm extends ARankingAlgorithm {
    double m_averageDocumentLength;//average document size in the corpus
    double k, b, avgDocLength;

    public BM25Algorithm(double weight, double b, double k) {
        super(weight);
        this.avgDocLength = 233;
        this.b = b;
        this.k = k;
    }

    public double rank(int wordInQueryCount, String documentName, int tf, double idf) {
        int documentLength = Model.documentDictionary.get(documentName).getDocLength();
        double numeratorBM25 = wordInQueryCount * (k+1) * tf * idf;
        double denominatorBM25 = tf+k * (1-b+(b * (documentLength / m_averageDocumentLength)));
        return numeratorBM25 / denominatorBM25 * weight;
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
