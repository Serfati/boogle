package ranker;

import indexer.InvertedIndex;
import javafx.util.Pair;
import parser.MiniDictionary;
import parser.cDocument;
import sun.awt.Mutex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

/**
 * ranker class . gets a list of terms and ranks all relevant documents by them by the weights set below
 */

public class Ranker implements IRanker {

    private double BM_25_B = 1.22, BM_25_K = 0.77, CONTAINING_WEIGHT = 0.4;
    private double IDF_DELTA = 1, TITLE_WEIGHT = 0.45, POSITIONS_WEIGHT = 0.08, BM25_WEIGHT = 0.25, IDF_LOWER_BOUND = 2;
    private int DOCUMENT_RETRIEVE_COUNT = 50;
    private Mutex rankMutex;
    private String termOutPath, docOutPath;
    private int blockSize;
    private double avgDocLength;
    private ArrayList<cDocument> docRanks;
    private ArrayList<cDocument> docBuffer;
    private HashMap<Integer, Pair> docPos;

    public Ranker(HashMap docPos, int blockSize) {
        this.avgDocLength = 233;
        this.termOutPath = termOutPath;
        this.docOutPath = docOutPath;
        this.blockSize = blockSize;
        this.docPos = docPos;
        this.docBuffer = new ArrayList<>();
        this.rankMutex = new Mutex();
        this.docRanks = new ArrayList<>();
    }

    /**
     * get the best scores and return an array filled with the document's names from the best
     * to the worst
     *
     * @param rankQ
     * @return
     */
    private ArrayList<cDocument> retrieveBestDocuments(PriorityQueue<cDocument> rankQ) {

        int i = 0;
        ArrayList<cDocument> bestDocumentsByOrder = new ArrayList<>();

        while(i < DOCUMENT_RETRIEVE_COUNT && rankQ.size() > 0) {
            bestDocumentsByOrder.add(i, rankQ.poll());
            i++;
        }

        return bestDocumentsByOrder;
    }


    private void dropTermsByIDF(ArrayList<InvertedIndex.Term> termList) {
    }

    private boolean idfMoreThanBound(double idf) {
        return true;
    }


    @Override
    public ArrayList<cDocument> rankByTerms(ArrayList<InvertedIndex.Term> termList) {
        return null;
    }

    @Override
    public void setAttributes(String termsPath, String docsPath, double docaAvgLength) {
        this.avgDocLength = avgDocLength;
        this.termOutPath = termsPath;
        this.docOutPath = docsPath;
    }

    @Override
    public void setDictionaries(HashMap<Integer, Pair> docPositions) {

    }

    private void addRank(MiniDictionary doc, double rank) {
        doc.addRank(rank);
    }
}
