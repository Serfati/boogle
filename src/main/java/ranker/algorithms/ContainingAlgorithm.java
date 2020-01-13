package ranker.algorithms;

import javafx.util.Pair;
import model.Model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

/* checks if a term is an entity of the given document */

public class ContainingAlgorithm extends ARankingAlgorithm {

    public ContainingAlgorithm(double weight) {
        super(weight);
    }

    public double rank(HashMap<String, Double> score, Set<String> wordsInQuery) {
        double rank = 0;
        for(String docs : score.keySet()) {
            Pair<String, Integer>[] five = Model.documentDictionary.get(docs).getFiveEntities();
            if (five != null)
                rank += Arrays.stream(five).filter(aFive -> aFive != null && wordsInQuery.contains(aFive.getKey())).mapToDouble(aFive -> 0.3).sum();
        }
        return rank * weight;
    }
}
