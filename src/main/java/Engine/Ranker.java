package Engine;
import Model.Model;

import java.util.HashMap;

public class Ranker {
    private HashMap<String, Integer> bagOfWords;
    private double documentAvgLenght;

    public Ranker( HashMap<String , Integer> bagOfWords){
        this.bagOfWords = bagOfWords;
        setAverageDoumnentLength();
    }

    double BM25( String word, String doDcumentNumber, int tf, int idf){
        double k = 1.2 ; double b = 0.75;
        int documentLength  = Model.documentDictionary.get(doDcumentNumber).getDocLength();
        int wordCountInQuery = bagOfWords.get(word);
        //TODO change tf
        double domBM25 = wordCountInQuery * tf * idf * ( k + 1 );
        double bm25 =  tf + k *( 1 - b + ( b * documentLength / documentAvgLenght ));
        return domBM25/bm25;
    }

    private void  setAverageDoumnentLength(){
        double sum = Model.documentDictionary.values().stream().mapToDouble(DocumentIndex::getDocLength).sum();
        documentAvgLenght =  ( sum / Model.documentDictionary.size() );
    }
}
